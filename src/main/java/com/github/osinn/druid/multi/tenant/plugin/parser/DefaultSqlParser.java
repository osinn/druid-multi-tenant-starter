package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

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

    /**
     * 多租户服务接口
     */
    private ITenantService tenantService;

    @Override
    public String setTenantParameter(String sql) {
        if (isEmpty(this.tenantInfoHandler.getTenantIds())) {
            return sql;
        }
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, tenantInfoHandler.getDbType());
        SQLStatement statement = statementList.get(0);
        if (statement instanceof SQLSelectStatement) {
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) statement;
            processSelectBody(sqlSelectStatement.getSelect().getQuery());
        } else if (statement instanceof SQLInsertStatement) {
            processInsert((SQLInsertStatement) statement);
        } else if (statement instanceof SQLUpdateStatement) {
            processUpdate((SQLUpdateStatement) statement);
        } else if (statement instanceof SQLDeleteStatement) {
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

                SQLExpr where = sqlSelectQueryBlock.getWhere();

                // 处理where 语句中多个in条件
                this.whereIn(where);
                String alias = this.getAlias(from);
                // 构造新的 where
                String tableName = null;
                if (from instanceof SQLExprTableSource) {
                    SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) from;
                    tableName = sqlExprTableSource.getExpr().toString();
                }

                // 是否包含tenant_id 为查询条件
                boolean isContainsTenantIdCondition = false;
                if (where instanceof SQLInListExpr) {
                    SQLInListExpr sqlExprTableSource = (SQLInListExpr) where;
                    isContainsTenantIdCondition = isContainsTenantIdCondition(sqlExprTableSource.getExpr());
                }

                if (!isContainsTenantIdCondition) {
                    SQLExpr newWhereCondition = this.createNewWhereCondition(tableName, where, alias);
                    sqlSelectQueryBlock.setWhere(newWhereCondition);
                }
            } else if (table instanceof SQLJoinTableSource) {
                SQLJoinTableSource joinTable = (SQLJoinTableSource) table;
                SQLTableSource left = joinTable.getLeft();
                SQLTableSource right = joinTable.getRight();

                this.joinCondition(left);
                this.joinCondition(right);
                String tableName = null;
                if (right instanceof SQLExprTableSource) {
                    SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) right;
                    tableName = sqlExprTableSource.getExpr().toString();
                }

                SQLExpr tenantCondition = getTenantCondition(tableName, right.getAlias(), joinTable.getCondition());
                if (tenantCondition != null) {
                    joinTable.addCondition(tenantCondition);
                }
                SQLExpr condition = joinTable.getCondition();
                // 处理where 语句中多个in条件
                this.whereIn(condition);

                String alias = this.getAlias(left);

                // 构造新的 where
                SQLExpr where = sqlSelectQueryBlock.getWhere();

                if (sqlSelectQueryBlock.getFrom() instanceof SQLExprTableSource) {
                    SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlSelectQueryBlock.getFrom();
                    tableName = sqlExprTableSource.getExpr().toString();
                } else if (left instanceof SQLExprTableSource) {
                    SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) left;
                    tableName = sqlExprTableSource.getExpr().toString();
                } else if (left instanceof SQLJoinTableSource) {
                    SQLJoinTableSource joinTableSource = (SQLJoinTableSource) left;
                    if (joinTableSource.getLeft() instanceof SQLExprTableSource) {
                        SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) joinTableSource.getLeft();
                        tableName = sqlExprTableSource.getExpr().toString();
                    }
                }

                SQLExpr newWhereCondition = this.createNewWhereCondition(tableName, where, alias);
                sqlSelectQueryBlock.setWhere(newWhereCondition);
            } else if (table instanceof SQLSubqueryTableSource) {
                // 例如 "select a,b from (select * from table_a) temp where temp.a = 'a';"
                // 子查询作为表
                SQLSubqueryTableSource subQueryTable = (SQLSubqueryTableSource) table;
                SQLSelectQuery query = subQueryTable.getSelect().getQuery();
                processSelectBody(query);
            } else if (table instanceof SQLUnionQueryTableSource) {
                SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) table;
                // 处理union的查询语句
                SQLUnionQuery sqlUnionQuery = sqlUnionQueryTableSource.getUnion();
                unionQuery(sqlUnionQuery);
            }

        } else if (sqlSelectQuery instanceof SQLUnionQuery) {
            // 处理union的查询语句
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            unionQuery(sqlUnionQuery);
        }
    }

    @Override
    public void processInsert(SQLInsertStatement insert) {
        List<SQLExpr> columns = insert.getColumns();
        for (SQLExpr column : columns) {
            if (isContainsTenantIdCondition(column)) {
                // 包含租户ID不再处理
                return;
            }
        }
        String tableName = insert.getTableName().toString();
        if (ignoreTable(tableName)) {
            return;
        }
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
        String tableName = sqlTableSource.toString();
        if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTable = (SQLJoinTableSource) sqlTableSource;
            SQLTableSource left = joinTable.getLeft();
            SQLTableSource right = joinTable.getRight();
            this.joinCondition(left);
            this.joinCondition(right);
            if (right instanceof SQLExprTableSource) {
                SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) right;
                tableName = sqlExprTableSource.getExpr().toString();
            }
            SQLExpr tenantCondition = getTenantCondition(tableName, right.getAlias(), joinTable.getCondition());
            if (tenantCondition != null) {
                joinTable.addCondition(tenantCondition);
            }

            SQLExpr condition = joinTable.getCondition();
            // 处理where 语句中多个in条件
            this.whereIn(condition);
            if (left instanceof SQLJoinTableSource) {
                alias = ((SQLJoinTableSource) left).getLeft().getAlias();
            } else {
                alias = left.getAlias();
            }
            if (left instanceof SQLExprTableSource) {
                tableName = ((SQLExprTableSource) left).getExpr().toString();
            }
        } else if (sqlTableSource instanceof SQLExprTableSource) {
            tableName = ((SQLExprTableSource) sqlTableSource).getExpr().toString();
        }

        SQLExpr tenantCondition = getTenantCondition(tableName, alias, update.getWhere());
        if (tenantCondition != null) {
            update.addCondition(tenantCondition);
        }
        SQLExpr where = update.getWhere();
        // 处理where 语句中多个in条件
        this.whereIn(where);

    }

    @Override
    public void processDelete(SQLDeleteStatement delete) {
        SQLTableSource tableSource = delete.getTableSource();
        String alias = tableSource.getAlias();
        String tableName = null;
        if (tableSource instanceof SQLJoinTableSource || delete.getFrom() instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTable;
            if (tableSource instanceof SQLJoinTableSource) {
                joinTable = (SQLJoinTableSource) tableSource;
            } else {
                joinTable = (SQLJoinTableSource) delete.getFrom();
            }
            SQLTableSource left = joinTable.getLeft();
            SQLTableSource right = joinTable.getRight();
            this.joinCondition(left);
            this.joinCondition(right);
            if (right instanceof SQLExprTableSource) {
                SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) right;
                tableName = sqlExprTableSource.getExpr().toString();
            }
            SQLExpr tenantCondition = getTenantCondition(tableName, right.getAlias(), joinTable.getCondition());
            if (tenantCondition != null) {
                joinTable.addCondition(tenantCondition);
            }
            SQLExpr condition = joinTable.getCondition();
            // 处理where 语句中多个in条件
            this.whereIn(condition);

            if (left instanceof SQLJoinTableSource) {
                alias = ((SQLJoinTableSource) left).getLeft().getAlias();
            } else {
                alias = left.getAlias();
            }

        } else if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) tableSource;
            tableName = sqlExprTableSource.getExpr().toString();
        }
        SQLExpr tenantCondition = getTenantCondition(tableName, alias, delete.getWhere());
        if (tenantCondition != null) {
            delete.addCondition(tenantCondition);
        }
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
            SQLTableSource left = sqlJoinTableSource.getLeft();
            SQLTableSource right = sqlJoinTableSource.getRight();
            String tableName = null;
            if (right instanceof SQLExprTableSource) {
                SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) right;
                tableName = sqlExprTableSource.getExpr().toString();
            } else if (left instanceof SQLExprTableSource) {
                SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) left;
                tableName = sqlExprTableSource.getExpr().toString();
            }
            SQLExpr tenantCondition = getTenantCondition(tableName, sqlJoinTableSource.getRight().getAlias(), sqlJoinTableSource.getCondition());

            if (tenantCondition != null) {
                sqlJoinTableSource.addCondition(tenantCondition);
            }
            joinCondition(left);
            joinCondition(right);
        } else if (sqlTableSource instanceof SQLSubqueryTableSource) {
            // 子查询作为表
            SQLSubqueryTableSource subQueryTable = (SQLSubqueryTableSource) sqlTableSource;
            SQLSelectQuery query = subQueryTable.getSelect().getQuery();
            processSelectBody(query);
        }
    }

    private SQLExpr createNewWhereCondition(String tableName, SQLExpr where, String alias) {
        // 如果是表达式
        if (where != null) {
            if (where instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr sqlExpr = (SQLBinaryOpExpr) where;
                SQLExpr right = sqlExpr.getRight();
                // 处理 where 条件中in查询
                whereIn(right);
            }

            SQLExpr tenantCondition = this.getTenantCondition(tableName, alias, where);
            if (tenantCondition == null) {
                return where;
            }
            // 构建新的 SQLBinaryOpExpr
            SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr();
            sqlBinaryOpExpr.setOperator(SQLBinaryOperator.BooleanAnd);
            sqlBinaryOpExpr.setParent(where.getParent());
            // 将左边设置成原来的
            sqlBinaryOpExpr.setLeft(where);
            sqlBinaryOpExpr.setRight(tenantCondition);
            // 返回新的条件
            return sqlBinaryOpExpr;
        } else {
            return this.getTenantCondition(tableName, alias, where);
        }
    }

    /**
     * 处理in条件查询
     *
     * @param sqlExpr
     */
    private void whereIn(SQLExpr sqlExpr) {
        if (sqlExpr instanceof SQLInSubQueryExpr) {
            SQLSelectQueryBlock selectQueryBlock = ((SQLInSubQueryExpr) sqlExpr).getSubQuery().getQueryBlock();
            if (selectQueryBlock != null) {
                processSelectBody(selectQueryBlock);
            }
        } else if (sqlExpr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) sqlExpr;
            this.whereIn(sqlBinaryOpExpr.getLeft());
            this.whereIn(sqlBinaryOpExpr.getRight());
        }
    }

    /**
     * 构建多租户字段条件
     *
     * @param alias 表别名
     * @return 返回条件
     */
    private SQLExpr getTenantCondition(String tableName, String alias, SQLExpr condition) {
        if (isContainsTenantIdCondition(condition) || ignoreTable(tableName) || ignoreTableAlias(alias)) {
            return null;
        }
        List<Object> tenantIds = this.tenantInfoHandler.getTenantIds();
        if (tenantIds.size() == 1) {
            return conditionEquality(alias, tenantIds.get(0));
        } else if (tenantIds.size() > 1) {
            return conditionIn(alias, tenantIds);
        } else {
            return null;
        }
    }

    private SQLBinaryOpExpr conditionEquality(String alias, Object tenantId) {
        SQLBinaryOpExpr tenantIdWhere = new SQLBinaryOpExpr();
        SQLPropertyExpr leftExpr = new SQLPropertyExpr();
        String tenantIdColumn = this.tenantInfoHandler.getTenantIdColumn();
        SQLExpr expr = SQLExprUtils.fromJavaObject(tenantId, null);
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
        if (alias != null) {
            // 如果有别名，则构造别名表达式
            SQLIdentifierExpr sqlIdentifierExpr = new SQLIdentifierExpr(alias);
            SQLPropertyExpr sqlPropertyExpr = new SQLPropertyExpr(sqlIdentifierExpr, this.tenantInfoHandler.getTenantIdColumn());
            sqlIdentifierExpr.setParent(sqlPropertyExpr);
            SQLInListExpr sqlInListExpr = new SQLInListExpr(sqlPropertyExpr);
            for (Object value : tenantIds) {
                sqlInListExpr.addTarget(SQLExprUtils.fromJavaObject(value, null));
            }
            return sqlInListExpr;
        } else {
            return SQLExprUtils.conditionIn(this.tenantInfoHandler.getTenantIdColumn(), tenantIds, null);
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
            if (where instanceof SQLPropertyExpr || where instanceof SQLIdentifierExpr) {
                return String.valueOf(where).contains(this.tenantInfoHandler.getTenantIdColumn());
            }
            return false;
        }
        SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) where;
        SQLExpr left = binaryOpExpr.getLeft();
        SQLExpr right = binaryOpExpr.getRight();
        // 是否包含tenant_id 为查询条件
        boolean isContainsTenantIdCondition = false;
        if (left instanceof SQLBinaryOpExpr || left instanceof SQLPropertyExpr) {
            isContainsTenantIdCondition = String.valueOf(left).contains(this.tenantInfoHandler.getTenantIdColumn());
        }
        if (right instanceof SQLBinaryOpExpr) {
            isContainsTenantIdCondition = String.valueOf(right).contains(this.tenantInfoHandler.getTenantIdColumn());
        }

        if (left instanceof SQLIdentifierExpr) {
            isContainsTenantIdCondition = String.valueOf(left).equals(this.tenantInfoHandler.getTenantIdColumn());
        }

        if (right instanceof SQLIdentifierExpr) {
            isContainsTenantIdCondition = String.valueOf(right).equals(this.tenantInfoHandler.getTenantIdColumn());
        }
        return isContainsTenantIdCondition;
    }

    /**
     * union 查询
     *
     * @param sqlUnionQuery
     */
    private void unionQuery(SQLUnionQuery sqlUnionQuery) {
        sqlUnionQuery.getRelations().forEach(this::processSelectBody);
    }

    /**
     * 根据表名判断是否需要忽略
     *
     * @param tableName 表名
     * @return
     */
    private boolean ignoreTable(String tableName) {
        if (tableName == null) {
            return false;
        }
        tableName = tableName.replace("`", "");
        List<String> ignoreTableNames = tenantInfoHandler.ignoreTableName();

        boolean ignoreTable = ignoreTableNamePrefix(tableName);
        if (ignoreTable) {
            return ignoreTable;
        }

        if (isEmpty(ignoreTableNames)) {
            return false;
        }

        for (String ignoreTableName : ignoreTableNames) {
            if (tableName.equals(ignoreTableName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据表名前缀判断是否需要忽略
     *
     * @param tableName 表名
     * @return
     */
    private boolean ignoreTableNamePrefix(String tableName) {

        List<String> ignoreTableNamePrefix = tenantInfoHandler.ignoreTableNamePrefix();

        if (isEmpty(ignoreTableNamePrefix)) {
            return false;
        }
        for (String tableNamePrefix : ignoreTableNamePrefix) {
            if (tableName.indexOf(tableNamePrefix) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据表别名判断是否需要忽略
     *
     * @param tableAlias 表别名
     * @return
     */
    private boolean ignoreTableAlias(String tableAlias) {
        if (tableAlias == null) {
            return false;
        }

        List<String> ignoreMatchTableAlias = tenantInfoHandler.ignoreMatchTableAlias();

        if (isEmpty(ignoreMatchTableAlias)) {
            return false;
        } else {
            return ignoreMatchTableAlias.stream().allMatch(tableAlias::contains);
        }
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
            } else if (sqlJoinTableSource.getLeft() instanceof SQLSubqueryTableSource) {
                SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource) sqlJoinTableSource.getLeft();
                return sqlSubqueryTableSource.getAlias();
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

    public void setTenantService(ITenantService tenantService) {
        this.tenantService = tenantService;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断数据源是否直接跳过租户ID设置
     *
     * @return
     */
    public boolean isIgnoreDynamicDatasource() {
        String ignoreDynamicDatasource = tenantService.ignoreDynamicDatasource();
        List<String> ignoreDynamicDatasourceList = tenantInfoHandler.ignoreDynamicDatasource();
        if (ignoreDynamicDatasource == null || ignoreDynamicDatasource.length() == 0 || isEmpty(ignoreDynamicDatasourceList)) {
            return false;
        }
        return ignoreDynamicDatasourceList.contains(ignoreDynamicDatasource);
    }
}
