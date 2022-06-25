package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.util.JdbcConstants;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.TimeZone;

/**
 * sql解析器实现
 *
 * @author wency_cai
 */
@Slf4j
public class DefaultSqlParser implements SqlParser {
    /**
     * 处理多租户信息处理器
     */
    private TenantInfoHandler tenantInfoHandler;

    @Override
    public String setTenantParameter(String sql) {
        DbType mysql = JdbcConstants.MYSQL;
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, mysql);
        SQLStatement statement = statementList.get(0);

        if (statement instanceof SQLSelectStatement) {
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) statement;
            processSelectBody(sqlSelectStatement.getSelect().getQuery());
        }
        if (statement instanceof SQLInsertStatement) {
            processInsert((SQLInsertStatement) statement);
        }
        if (statement instanceof SQLUpdateStatement) {
            processUpdate((SQLUpdateStatement) statement);
        }
        if (statement instanceof SQLDeleteStatement) {
            processDelete((SQLDeleteStatement) statement);
        }
        return statement.toString();
    }

    @Override
    public void processSelectBody(SQLSelectQuery sqlSelectQuery) {
        // 非union的查询语句
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            List<SQLSelectItem> selectList = sqlSelectQueryBlock.getSelectList();
            // 处理select 子查询
            selectList.forEach(sqlSelectItem -> {
                if (sqlSelectItem.getExpr() instanceof SQLQueryExpr) {
                    SQLQueryExpr expr = (SQLQueryExpr) sqlSelectItem.getExpr();
                    SQLSelectQuery query = expr.getSubQuery().getQuery();
                    processSelectBody(query);
                } else if (sqlSelectItem.getExpr() instanceof SQLExistsExpr) {
                    // 处理exists查询
                    SQLExistsExpr sqlExistsExpr = (SQLExistsExpr) sqlSelectItem.getExpr();
                    SQLSelectQuery query = sqlExistsExpr.getSubQuery().getQuery();
                    processSelectBody(query);
                }
            });

            // 获取表
            SQLTableSource table = sqlSelectQueryBlock.getFrom();
            if (table instanceof SQLExprTableSource) {
                SQLTableSource from = sqlSelectQueryBlock.getFrom();
//                boolean containsTenantIdCondition = isContainsTenantIdCondition(where);

                SQLExpr where = sqlSelectQueryBlock.getWhere();

//                SQLBinaryOpExpr tenantCondition = getTenantCondition(from.getAlias());
//                sqlSelectQueryBlock.addCondition(tenantCondition);

                // 处理where 语句中多个in条件
                this.whereIn(where);
                String alias = this.getAlias(from);
                // 构造新的 where
                SQLExpr newWhereCondition = this.createNewWhereCondition(where, alias);
                sqlSelectQueryBlock.setWhere(newWhereCondition);

            } else if (table instanceof SQLJoinTableSource) {
                SQLJoinTableSource joinTable = (SQLJoinTableSource) table;
                SQLTableSource left = joinTable.getLeft();
                SQLTableSource right = joinTable.getRight();

                this.joinCondition(left);
                this.joinCondition(right);
                SQLExpr tenantCondition = getTenantCondition(right.getAlias());
                joinTable.addCondition(tenantCondition);
                SQLExpr condition = joinTable.getCondition();
                // 处理where 语句中多个in条件
                this.whereIn(condition);

                String alias = this.getAlias(left);

                // 构造新的 where
                SQLExpr where = sqlSelectQueryBlock.getWhere();
                SQLExpr newWhereCondition = this.createNewWhereCondition(where, alias);
                sqlSelectQueryBlock.setWhere(newWhereCondition);
            } else if (table instanceof SQLSubqueryTableSource) {
                // 例如 "select a,b from (select * from table_a) temp where temp.a = 'a';"
                // 子查询作为表
                SQLSubqueryTableSource subQueryTable = (SQLSubqueryTableSource) table;
                SQLSelectQuery query = subQueryTable.getSelect().getQuery();
                processSelectBody(query);
            }

        } else if (sqlSelectQuery instanceof SQLUnionQuery) {
            // 处理union的查询语句
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            sqlUnionQuery.getRelations().forEach(this::processSelectBody);
        }
    }

    @Override
    public void processInsert(SQLInsertStatement insert) {
        List<Object> tenantIds = this.tenantInfoHandler.getTenantIds();
        if (tenantIds == null || tenantIds.size() == 0) {
            return;
        }
        insert.addColumn(new SQLIdentifierExpr(this.tenantInfoHandler.getTenantIdColumn()));
        Object tenantId = tenantIds.get(0);
        insert.getValuesList().forEach(valuesClause -> valuesClause.addValue(tenantId));
    }

    @Override
    public void processUpdate(SQLUpdateStatement update) {
        SQLTableSource sqlTableSource = update.getTableSource();
        String alias = sqlTableSource.getAlias();
        if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTable = (SQLJoinTableSource) sqlTableSource;
            SQLTableSource left = joinTable.getLeft();
            SQLTableSource right = joinTable.getRight();
            this.joinCondition(left);
            this.joinCondition(right);
            SQLExpr tenantCondition = getTenantCondition(right.getAlias());
            joinTable.addCondition(tenantCondition);

            SQLExpr condition = joinTable.getCondition();
            // 处理where 语句中多个in条件
            this.whereIn(condition);
            if (left instanceof SQLJoinTableSource) {
                alias = ((SQLJoinTableSource) left).getLeft().getAlias();
            } else {
                alias = left.getAlias();
            }
        }
        SQLExpr tenantCondition = getTenantCondition(alias);
        update.addCondition(tenantCondition);
        SQLExpr where = update.getWhere();
        // 处理where 语句中多个in条件
        this.whereIn(where);

    }

    @Override
    public void processDelete(SQLDeleteStatement delete) {
        SQLTableSource tableSource = delete.getTableSource();
        String alias = tableSource.getAlias();
        if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTable = (SQLJoinTableSource) tableSource;
            SQLTableSource left = joinTable.getLeft();
            SQLTableSource right = joinTable.getRight();
            this.joinCondition(left);
            this.joinCondition(right);
            SQLExpr tenantCondition = getTenantCondition(right.getAlias());
            joinTable.addCondition(tenantCondition);
            SQLExpr condition = joinTable.getCondition();
            // 处理where 语句中多个in条件
            this.whereIn(condition);

            if (left instanceof SQLJoinTableSource) {
                alias = ((SQLJoinTableSource) left).getLeft().getAlias();
            } else {
                alias = left.getAlias();
            }
        }

        SQLExpr tenantCondition = getTenantCondition(alias);
        delete.addCondition(tenantCondition);
        SQLExpr where = delete.getWhere();
        // 处理where 语句中多个in条件
        this.whereIn(where);

    }

    /**
     * 多表关联查询 on 添加字段
     *
     * @param sqlTableSource
     */
    private void joinCondition(SQLTableSource sqlTableSource) {
        if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            SQLExpr tenantCondition = getTenantCondition(sqlJoinTableSource.getRight().getAlias());
            sqlJoinTableSource.addCondition(tenantCondition);

            SQLTableSource left = sqlJoinTableSource.getLeft();
            SQLTableSource right = sqlJoinTableSource.getRight();
            joinCondition(left);
            joinCondition(right);
        } else if (sqlTableSource instanceof SQLSubqueryTableSource) {
            // 子查询作为表
            SQLSubqueryTableSource subQueryTable = (SQLSubqueryTableSource) sqlTableSource;
            SQLSelectQuery query = subQueryTable.getSelect().getQuery();
            processSelectBody(query);
        }
    }

    private SQLExpr createNewWhereCondition(SQLExpr where, String alias) {
        // 如果是表达式
        if (where != null) {
            // 构建新的 SQLBinaryOpExpr
            SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr();
            sqlBinaryOpExpr.setOperator(SQLBinaryOperator.BooleanAnd);
            sqlBinaryOpExpr.setParent(where.getParent());
            // 将左边设置成原来的
            sqlBinaryOpExpr.setLeft(where);
            SQLExpr tenantCondition = this.getTenantCondition(alias);
            sqlBinaryOpExpr.setRight(tenantCondition);
            // 返回新的条件
            return sqlBinaryOpExpr;
        } else {
            return this.getTenantCondition(alias);
        }
    }

    /**
     * 处理in条件查询
     *
     * @param sqlExpr
     */
    private void whereIn(SQLExpr sqlExpr) {
        if (isContainsTenantIdCondition(sqlExpr)) {
            return;
        }
        if (sqlExpr instanceof SQLInSubQueryExpr) {
            SQLSelectQueryBlock selectQueryBlock = ((SQLInSubQueryExpr) sqlExpr).getSubQuery().getQueryBlock();
            if (selectQueryBlock != null) {
                SQLExpr tenantCondition = getTenantCondition(selectQueryBlock.getFrom().getAlias());
                selectQueryBlock.addCondition(tenantCondition);
            }
        } else if (sqlExpr instanceof SQLBinaryOpExpr) {
            this.whereIn(((SQLBinaryOpExpr) sqlExpr).getLeft());
            this.whereIn(((SQLBinaryOpExpr) sqlExpr).getRight());
        }
    }

    /**
     * 构建多租户字段条件
     *
     * @param alias 表别名
     * @return 返回条件
     */
    private SQLExpr getTenantCondition(String alias) {
//        if (isContainsTenantIdCondition(sqlExpr)) {
//            return null;
//        }
        List<Object> tenantIds = this.tenantInfoHandler.getTenantIds();
        if (tenantIds.size() == 1) {
            return getEqualityCondition(alias, tenantIds.get(0));
        } else {
            return conditionIn(alias, tenantIds);
        }
    }

    private SQLBinaryOpExpr getEqualityCondition(String alias, Object tenantId) {
        SQLBinaryOpExpr tenantIdWhere = new SQLBinaryOpExpr();
        SQLPropertyExpr leftExpr = new SQLPropertyExpr();
        String tenantIdColumn = this.tenantInfoHandler.getTenantIdColumn();
        SQLExpr expr = SQLExprUtils.fromJavaObject(tenantId, TimeZone.getDefault());
        if (alias != null) {
            leftExpr.setOwner(alias);
            leftExpr.setName(tenantIdColumn);
            tenantIdWhere.setLeft(leftExpr);
            tenantIdWhere.setRight(expr);
            tenantIdWhere.setOperator(SQLBinaryOperator.Equality);
        } else {
            // 拼接新的条件
            tenantIdWhere.setOperator(SQLBinaryOperator.Equality);
            tenantIdWhere.setLeft(new SQLIdentifierExpr(tenantIdColumn));
            // 设置当前租户ID条件
            tenantIdWhere.setRight(expr);
        }
        return tenantIdWhere;
    }

    private SQLInListExpr conditionIn(String alias, List<Object> tenantIds) {
        TimeZone timeZone = TimeZone.getDefault();
        if (alias != null) {
            // 如果有别名，则构造别名表达式
            SQLIdentifierExpr sqlIdentifierExpr = new SQLIdentifierExpr(alias);
            SQLPropertyExpr sqlPropertyExpr = new SQLPropertyExpr(sqlIdentifierExpr, this.tenantInfoHandler.getTenantIdColumn());
            sqlIdentifierExpr.setParent(sqlPropertyExpr);
            SQLInListExpr sqlInListExpr = new SQLInListExpr(sqlPropertyExpr);
            for (Object value : tenantIds) {
                sqlInListExpr.addTarget(SQLExprUtils.fromJavaObject(value, timeZone));
            }
            return sqlInListExpr;
        } else {
            return SQLExprUtils.conditionIn(this.tenantInfoHandler.getTenantIdColumn(), tenantIds, timeZone);
        }
    }

    /**
     * 条件中是否为 and or 表达式
     *
     * @param where sql中where条件语句
     * @return 判断结果
     */
    private boolean isContainsTenantIdCondition(SQLExpr where) {
        if (!(where instanceof SQLBinaryOpExpr)) {
            return false;
        }
        SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) where;
        SQLExpr left = binaryOpExpr.getLeft();
        SQLExpr right = binaryOpExpr.getRight();
        // 是否包含tenant_id 为查询条件
        if (!(left instanceof SQLBinaryOpExpr) && !(right instanceof SQLBinaryOpExpr)
                && (this.tenantInfoHandler.getTenantIdColumn().equals(String.valueOf(left))
                || this.tenantInfoHandler.getTenantIdColumn().equals(String.valueOf(right)))) {
            return true;
        }
        return false;
    }

    /**
     * 是否包括 or tenant_id = xx的条件
     *
     * @param where sql中where条件语句
     * @return 判断结果
     */
    private boolean isTenantIdAndOrCondition(SQLExpr where) {
        if (!(where instanceof SQLBinaryOpExpr)) {
            return false;
        }
        SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) where;
        if ((isContainsTenantIdCondition(binaryOpExpr.getLeft())
                || isContainsTenantIdCondition(binaryOpExpr.getRight()))
                && "BooleanOr".equals(String.valueOf(binaryOpExpr.getOperator()))) {
            return true;
        }
        return isTenantIdAndOrCondition(binaryOpExpr.getLeft()) || isTenantIdAndOrCondition(binaryOpExpr.getRight());
    }

    /**
     * 获取别名
     * 若果是多表查询获取第一个表的别名
     *
     * @param sqlTableSource
     * @return
     */
    private String getAlias(SQLTableSource sqlTableSource) {
        if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            if (sqlJoinTableSource.getLeft() instanceof SQLJoinTableSource) {
                return getAlias(sqlJoinTableSource.getLeft());
            } else if (sqlJoinTableSource.getLeft() instanceof SQLExprTableSource) {
                SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlJoinTableSource.getLeft();
                return sqlExprTableSource.getAlias();
            } else {
                return sqlJoinTableSource.getAlias();
            }
        } else if (sqlTableSource instanceof SQLExprTableSource) {
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            return sqlExprTableSource.getAlias();
        } else if (sqlTableSource != null) {
            return sqlTableSource.getAlias();
        } else {
            return null;
        }
    }

    public void setTenantInfoHandler(TenantInfoHandler tenantInfoHandler) {
        this.tenantInfoHandler = tenantInfoHandler;
    }
}
