package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.util.JdbcUtils;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * sql解析器实现
 *
 * @author wency_cai
 */
@Slf4j
public class DefaultSqlParser implements SqlParser {

    /**
     * CTE 名称不是真实表，引用它时不应该再拼接租户条件；
     * <ul>
     *     <li>非递归 CTE 的别名对它自身的定义体不可见（体内同名标识符指向真实表），只对其后定义的 CTE 以及主语句可见；</li>
     *     <li>递归 CTE（with recursive）的别名对自身定义体也可见；</li>
     *     <li>内层 with 的别名出了内层语句即失效。</li>
     * </ul>
     */
    private static final ThreadLocal<Set<String>> CTE_ALIASES = new ThreadLocal<>();

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
        return setTenantParameter(null, sql, null);
    }

    @Override
    public String setTenantParameter(String url, String sql, Object paramTenantId) {

        if (sql == null || sql.length() == 0) {
            return sql;
        }

        String customizeParserSQL = tenantService.customizeParser(sql, paramTenantId);

        if (customizeParserSQL != null && customizeParserSQL.length() > 0) {
            return customizeParserSQL;
        }

        String beforeSql = tenantService.before(sql, paramTenantId);
        if (beforeSql != null && beforeSql.length() > 0) {
            sql = beforeSql;
        }

        DbType dbType = tenantInfoHandler.getDbType();

        if (dbType == null) {
            // 获取当前数据库方言，一般在多数据源模式下获取当前数据源的数据库方言
            dbType = JdbcUtils.getDbTypeRaw(url, null);
        }

        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);

        Set<String> previousCteAliases = CTE_ALIASES.get();
        String parserSql;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            // 支持多语句情况
            for (SQLStatement statement : statementList) {
                processStatement(statement, paramTenantId);
                stringBuilder.append(statement.toString());
            }
            parserSql = stringBuilder.toString();
        } finally {
            if (previousCteAliases == null) {
                CTE_ALIASES.remove();
            } else {
                CTE_ALIASES.set(previousCteAliases);
            }
        }

        String afterSql = tenantService.after(parserSql, paramTenantId);
        if (afterSql != null && afterSql.length() > 0) {
            return afterSql;
        } else {
            return parserSql;
        }
    }

    /**
     * 处理单条 SQL 语句（含 with 子句）
     */
    private void processStatement(SQLStatement statement, Object paramTenantId) {
        if (statement instanceof SQLSelectStatement) {
            processSelect(((SQLSelectStatement) statement).getSelect(), paramTenantId);
        } else if (statement instanceof SQLInsertStatement) {
            SQLInsertStatement insert = (SQLInsertStatement) statement;
            processWithScope(insert.getWith(), paramTenantId, () -> processInsert(insert, paramTenantId));
        } else if (statement instanceof SQLUpdateStatement) {
            SQLUpdateStatement update = (SQLUpdateStatement) statement;
            processWithScope(update.getWith(), paramTenantId, () -> processUpdate(update, paramTenantId));
        } else if (statement instanceof SQLDeleteStatement) {
            SQLDeleteStatement delete = (SQLDeleteStatement) statement;
            processWithScope(delete.getWith(), paramTenantId, () -> processDelete(delete, paramTenantId));
        }
    }

    /**
     * 处理带 with 子句的查询：先让 with 中定义的 CTE 别名按顺序生效，再处理查询主体，
     * 结束后回收本层新增的别名。
     */
    private void processSelect(SQLSelect select, Object paramTenantId) {
        if (select == null) {
            return;
        }
        processWithScope(select.getWithSubQuery(), paramTenantId,
                () -> processSelectBody(select.getQuery(), paramTenantId));
    }

    /**
     * 在 with 子句定义的作用域内执行语句主体。
     * <p>
     * with 中定义的别名对「其后的 CTE」和「主语句」可见，出了本层立即失效。
     *
     * @param withSubQuery  with 子句
     * @param paramTenantId mapper 接口传入的租户ID
     * @param statementBody 语句主体处理逻辑
     */
    private void processWithScope(SQLWithSubqueryClause withSubQuery, Object paramTenantId, Runnable statementBody) {
        // 只有创建了 ThreadLocal 的那一层负责清理，
        // 否则内层空作用域会把别名集合摘掉，导致外层后续新增的别名写进孤儿集合而全部失效
        boolean ownsAliasContext = CTE_ALIASES.get() == null;
        Set<String> visibleAliases = cteAliases();
        Set<String> addedAliases = new HashSet<>();
        try {
            processWithClause(withSubQuery, paramTenantId, addedAliases);
            statementBody.run();
        } finally {
            visibleAliases.removeAll(addedAliases);
            if (ownsAliasContext && visibleAliases.isEmpty()) {
                CTE_ALIASES.remove();
            }
        }
    }

    /**
     * 按定义顺序处理 with 子句，CTE 内部的真实表同样需要拼接租户条件。
     *
     * @param withSubQuery  with 子句
     * @param paramTenantId mapper 接口传入的租户ID
     * @param addedAliases  本层新增的别名，用于离开作用域时回收
     */
    private void processWithClause(SQLWithSubqueryClause withSubQuery, Object paramTenantId, Set<String> addedAliases) {
        if (withSubQuery == null || withSubQuery.getEntries() == null || withSubQuery.getEntries().isEmpty()) {
            return;
        }
        Set<String> visibleAliases = cteAliases();
        // 递归 CTE 的名称对自身定义体也可见
        boolean recursive = Boolean.TRUE.equals(withSubQuery.getRecursive());
        for (SQLWithSubqueryClause.Entry entry : withSubQuery.getEntries()) {
            if (entry == null) {
                continue;
            }
            String alias = toCteKey(entry.getAlias());
            if (recursive && alias != null && visibleAliases.add(alias)) {
                addedAliases.add(alias);
            }
            processSelect(entry.getSubQuery(), paramTenantId);
            if (!recursive && alias != null && visibleAliases.add(alias)) {
                addedAliases.add(alias);
            }
        }
    }

    /**
     * 获取当前线程的 CTE 别名集合（不存在则创建）
     */
    private Set<String> cteAliases() {
        Set<String> aliases = CTE_ALIASES.get();
        if (aliases == null) {
            aliases = new HashSet<>();
            CTE_ALIASES.set(aliases);
        }
        return aliases;
    }

    /**
     * CTE 别名比较用的键：去掉引用符号并忽略大小写
     */
    private static String toCteKey(String name) {
        if (name == null) {
            return null;
        }
        return removeIdentifierQuote(name).toLowerCase(Locale.ROOT);
    }

    @Override
    public void processSelectBody(SQLSelectQuery sqlSelectQuery, Object paramTenantId) {
        // 非union的查询语句
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            List<SQLSelectItem> selectList = sqlSelectQueryBlock.getSelectList();
            // 处理select 子查询
            selectList.forEach(sqlSelectItem -> {
                if (sqlSelectItem.getExpr() instanceof SQLQueryExpr) {
                    SQLQueryExpr expr = (SQLQueryExpr) sqlSelectItem.getExpr();
                    SQLSelectQuery query = expr.getSubQuery().getQuery();
                    processSelectBody(query, paramTenantId);
                } else if (sqlSelectItem.getExpr() instanceof SQLExistsExpr) {
                    // 处理exists查询
                    SQLExistsExpr sqlExistsExpr = (SQLExistsExpr) sqlSelectItem.getExpr();
                    SQLSelectQuery query = sqlExistsExpr.getSubQuery().getQuery();
                    processSelectBody(query, paramTenantId);
                }
            });

            // 获取表
            SQLTableSource table = sqlSelectQueryBlock.getFrom();
            if (table instanceof SQLExprTableSource) {
                SQLTableSource from = sqlSelectQueryBlock.getFrom();

                SQLExpr where = sqlSelectQueryBlock.getWhere();

                // 处理where 语句中多个in条件
                this.whereIn(where, paramTenantId);
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
                } else {
                    isContainsTenantIdCondition = isContainsTenantIdCondition(where);
                }

                if (!isContainsTenantIdCondition) {
                    SQLExpr newWhereCondition = this.createNewWhereCondition(tableName, where, alias, paramTenantId);
                    sqlSelectQueryBlock.setWhere(newWhereCondition);
                }
            } else if (table instanceof SQLJoinTableSource) {
                SQLJoinTableSource joinTable = (SQLJoinTableSource) table;
                SQLTableSource left = joinTable.getLeft();
                SQLTableSource right = joinTable.getRight();

                this.joinCondition(left, paramTenantId);
                this.joinCondition(right, paramTenantId);
                String tableName = null;
                if (right instanceof SQLExprTableSource) {
                    SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) right;
                    tableName = sqlExprTableSource.getExpr().toString();
                }

                SQLExpr tenantCondition = getTenantCondition(tableName, right.getAlias(), joinTable.getCondition(), paramTenantId);
                if (tenantCondition != null) {
                    // 原 on 条件顶层是 OR/XOR 时先加括号，避免与 AND 组合时语义被改变
                    parenthesizeIfLogicalOr(joinTable.getCondition());
                    joinTable.addCondition(tenantCondition);
                }
                SQLExpr condition = joinTable.getCondition();
                // 处理where 语句中多个in条件
                this.whereIn(condition, paramTenantId);

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
                    SQLExprTableSource exprTableSource = lastJoinTableSourceLeadTable(joinTableSource);
                    if (exprTableSource != null) {
                        tableName = exprTableSource.getTableName();
                        alias = exprTableSource.getAlias();
                    }
                }

                SQLExpr newWhereCondition = this.createNewWhereCondition(tableName, where, alias, paramTenantId);
                sqlSelectQueryBlock.setWhere(newWhereCondition);
            } else if (table instanceof SQLSubqueryTableSource) {
                // 例如 "select a,b from (select * from table_a) temp where temp.a = 'a';"
                // 子查询作为表，其自身也可能带 with 子句
                SQLSubqueryTableSource subQueryTable = (SQLSubqueryTableSource) table;
                processSelect(subQueryTable.getSelect(), paramTenantId);
            } else if (table instanceof SQLUnionQueryTableSource) {
                SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) table;
                // 处理union的查询语句
                SQLUnionQuery sqlUnionQuery = sqlUnionQueryTableSource.getUnion();
                unionQuery(sqlUnionQuery, paramTenantId);
            }

        } else if (sqlSelectQuery instanceof SQLUnionQuery) {
            // 处理union的查询语句
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
            unionQuery(sqlUnionQuery, paramTenantId);
        }
    }

    @Override
    public void processInsert(SQLInsertStatement insert, Object paramTenantId) {
        String tenantIdColumn = this.tenantInfoHandler.getTenantIdColumn();
        List<SQLExpr> columns = insert.getColumns();

        // insert 语句中是否包含租户ID字段, 例如 INSERT INTO role (id, `name`, tenant_id)
        boolean isInsertFieldExistTenantIdColumn = false;
        for (SQLExpr column : columns) {
            if (isTenantIdColumnExpr(column)) {
                isInsertFieldExistTenantIdColumn = true;
                break;
            }
        }

        if (!isInsertFieldExistTenantIdColumn) {
            String tableName = insert.getTableName().toString();
            if (ignoreTable(tableName)) {
                return;
            }
            List<Object> tenantIds = this.getTenantId(paramTenantId);
            if (tenantIds.size() == 0) {
                return;
            }
            // 如果insert 没有指定租户ID字段，认为是插入全部字段包括租户ID，不需要额外处理添加租户ID字段，否则需要添加
            if (!columns.isEmpty()) {
                insert.addColumn(new SQLIdentifierExpr(tenantIdColumn));
                Object tenantId = tenantIds.get(0);
                insert.getValuesList().forEach(valuesClause -> valuesClause.addValue(tenantId));
            }
        }
        // insert 语句中包含tenant_id字段，也要检查是否使用了insert select语法
        this.handlerInsertSelect(insert, tenantIdColumn, paramTenantId);
    }

    @Override
    public void processUpdate(SQLUpdateStatement update, Object paramTenantId) {
        SQLTableSource sqlTableSource = update.getTableSource();
        String alias = sqlTableSource.getAlias();
        String tableName = sqlTableSource.toString();
        if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTable = (SQLJoinTableSource) sqlTableSource;
            SQLTableSource left = joinTable.getLeft();
            SQLTableSource right = joinTable.getRight();
            this.joinCondition(left, paramTenantId);
            this.joinCondition(right, paramTenantId);
            if (right instanceof SQLExprTableSource) {
                SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) right;
                tableName = sqlExprTableSource.getExpr().toString();
            }
            SQLExpr tenantCondition = getTenantCondition(tableName, right.getAlias(), joinTable.getCondition(), paramTenantId);
            if (tenantCondition != null) {
                // 原 on 条件顶层是 OR/XOR 时先加括号，避免与 AND 组合时语义被改变
                parenthesizeIfLogicalOr(joinTable.getCondition());
                joinTable.addCondition(tenantCondition);
            }

            SQLExpr condition = joinTable.getCondition();
            // 处理where 语句中多个in条件
            this.whereIn(condition, paramTenantId);
            if (left instanceof SQLJoinTableSource) {
                alias = ((SQLJoinTableSource) left).getLeft().getAlias();
            } else {
                alias = left.getAlias();
            }
            if (left instanceof SQLExprTableSource) {
                tableName = ((SQLExprTableSource) left).getExpr().toString();
            } else if (left instanceof SQLJoinTableSource) {
                SQLJoinTableSource joinTableSource = (SQLJoinTableSource) left;
                SQLExprTableSource exprTableSource = lastJoinTableSourceLeadTable(joinTableSource);
                if (exprTableSource != null) {
                    tableName = exprTableSource.getTableName();
                    alias = exprTableSource.getAlias();
                }
            }

        } else if (sqlTableSource instanceof SQLExprTableSource) {
            tableName = ((SQLExprTableSource) sqlTableSource).getExpr().toString();
        }

        SQLExpr tenantCondition = getTenantCondition(tableName, alias, update.getWhere(), paramTenantId);
        if (tenantCondition != null) {
            // 原 where 顶层是 OR/XOR 时先加括号，避免与 AND 组合时语义被改变
            parenthesizeIfLogicalOr(update.getWhere());
            update.addCondition(tenantCondition);
        }
        SQLExpr where = update.getWhere();
        // 处理where 语句中多个in条件
        this.whereIn(where, paramTenantId);

    }

    @Override
    public void processDelete(SQLDeleteStatement delete, Object paramTenantId) {
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
            this.joinCondition(left, paramTenantId);
            this.joinCondition(right, paramTenantId);
            if (right instanceof SQLExprTableSource) {
                SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) right;
                tableName = sqlExprTableSource.getExpr().toString();
            }
            SQLExpr tenantCondition = getTenantCondition(tableName, right.getAlias(), joinTable.getCondition(), paramTenantId);
            if (tenantCondition != null) {
                // 原 on 条件顶层是 OR/XOR 时先加括号，避免与 AND 组合时语义被改变
                parenthesizeIfLogicalOr(joinTable.getCondition());
                joinTable.addCondition(tenantCondition);
            }
            SQLExpr condition = joinTable.getCondition();
            // 处理where 语句中多个in条件
            this.whereIn(condition, paramTenantId);

            if (left instanceof SQLJoinTableSource) {
                alias = ((SQLJoinTableSource) left).getLeft().getAlias();
            } else {
                alias = left.getAlias();
            }
            if (left instanceof SQLExprTableSource) {
                tableName = ((SQLExprTableSource) left).getExpr().toString();
            } else if (left instanceof SQLJoinTableSource) {
                SQLJoinTableSource joinTableSource = (SQLJoinTableSource) left;
                SQLExprTableSource exprTableSource = lastJoinTableSourceLeadTable(joinTableSource);
                if (exprTableSource != null) {
                    tableName = exprTableSource.getTableName();
                    alias = exprTableSource.getAlias();
                }
            }
        } else if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) tableSource;
            tableName = sqlExprTableSource.getExpr().toString();
        }

        SQLExpr tenantCondition = getTenantCondition(tableName, alias, delete.getWhere(), paramTenantId);
        if (tenantCondition != null) {
            // 原 where 顶层是 OR/XOR 时先加括号，避免与 AND 组合时语义被改变
            parenthesizeIfLogicalOr(delete.getWhere());
            delete.addCondition(tenantCondition);
        }
        SQLExpr where = delete.getWhere();
        // 处理where 语句中多个in条件
        this.whereIn(where, paramTenantId);

    }

    /**
     * 多表关联查询 on 添加字段
     *
     * @param sqlTableSource
     */
    private void joinCondition(SQLTableSource sqlTableSource, Object paramTenantId) {
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
            SQLExpr tenantCondition = getTenantCondition(tableName, sqlJoinTableSource.getRight().getAlias(), sqlJoinTableSource.getCondition(), paramTenantId);

            if (tenantCondition != null) {
                sqlJoinTableSource.addCondition(tenantCondition);
            }
            joinCondition(left, paramTenantId);
            joinCondition(right, paramTenantId);
        } else if (sqlTableSource instanceof SQLSubqueryTableSource) {
            // 子查询作为表，其自身也可能带 with 子句
            SQLSubqueryTableSource subQueryTable = (SQLSubqueryTableSource) sqlTableSource;
            processSelect(subQueryTable.getSelect(), paramTenantId);
        } else if (sqlTableSource instanceof SQLUnionQueryTableSource) {
            SQLUnionQueryTableSource sqlUnionQueryTableSource = (SQLUnionQueryTableSource) sqlTableSource;
            // 处理union的查询语句
            SQLUnionQuery sqlUnionQuery = sqlUnionQueryTableSource.getUnion();
            unionQuery(sqlUnionQuery, paramTenantId);
        }
    }

    private SQLExpr createNewWhereCondition(String tableName, SQLExpr where, String alias, Object paramTenantId) {
        // 如果是表达式
        if (where != null) {
            if (where instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr sqlExpr = (SQLBinaryOpExpr) where;
                SQLExpr right = sqlExpr.getRight();
                // 处理 where 条件中in查询
                whereIn(right, paramTenantId);
            }

            SQLExpr tenantCondition = this.getTenantCondition(tableName, alias, where, paramTenantId);
            if (tenantCondition == null) {
                return where;
            }
            // 原条件顶层是 OR/XOR 时必须先加括号，否则 AND 优先级更高会改变语义（导致租户隔离失效）
            parenthesizeIfLogicalOr(where);
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
            return this.getTenantCondition(tableName, alias, where, paramTenantId);
        }
    }

    /**
     * 条件顶层是 OR/XOR 时标记为带括号，避免与 AND 组合时因运算符优先级改变语义。
     *
     * @param condition 原始条件
     */
    private void parenthesizeIfLogicalOr(SQLExpr condition) {
        if (condition instanceof SQLBinaryOpExpr) {
            SQLBinaryOperator operator = ((SQLBinaryOpExpr) condition).getOperator();
            if (SQLBinaryOperator.BooleanOr == operator || SQLBinaryOperator.BooleanXor == operator) {
                ((SQLBinaryOpExpr) condition).setParenthesized(true);
            }
        }
    }

    /**
     * 处理in条件查询
     *
     * @param sqlExpr
     */
    private void whereIn(SQLExpr sqlExpr, Object paramTenantId) {
        if (sqlExpr == null) {
            return;
        }
        if (sqlExpr instanceof SQLInSubQueryExpr) {
            processSubQuery(((SQLInSubQueryExpr) sqlExpr).getSubQuery(), paramTenantId);
        } else if (sqlExpr instanceof SQLExistsExpr) {
            // 处理 exists 查询，例如 where exists (select 1 from role r where r.id = u.role_id)
            processSubQuery(((SQLExistsExpr) sqlExpr).getSubQuery(), paramTenantId);
        } else if (sqlExpr instanceof SQLQueryExpr) {
            // 处理标量子查询，例如 where (select count(*) from role r) > 1
            processSubQuery(((SQLQueryExpr) sqlExpr).getSubQuery(), paramTenantId);
        } else if (sqlExpr instanceof SQLNotExpr) {
            // 处理 not 表达式，例如 not exists (...)
            this.whereIn(((SQLNotExpr) sqlExpr).getExpr(), paramTenantId);
        } else if (sqlExpr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) sqlExpr;
            this.whereIn(sqlBinaryOpExpr.getLeft(), paramTenantId);
            this.whereIn(sqlBinaryOpExpr.getRight(), paramTenantId);
        }
    }

    /**
     * 处理子查询（in 子查询 / exists 子查询 / 标量子查询）
     *
     * @param subQuery 子查询
     */
    private void processSubQuery(SQLSelect subQuery, Object paramTenantId) {
        // 子查询自身也可能带 with 子句，统一走带作用域的入口
        processSelect(subQuery, paramTenantId);
    }

    /**
     * 构建多租户字段条件
     *
     * @param alias 表别名
     * @return 返回条件
     */
    private SQLExpr getTenantCondition(String tableName, String alias, SQLExpr condition, Object paramTenantId) {
        if (isContainsTenantIdCondition(condition) || ignoreTable(tableName) || ignoreTableAlias(alias)) {
            return null;
        }
        List<Object> tenantIds = this.getTenantId(paramTenantId);
        if (tenantIds.size() == 1) {
            return conditionEquality(alias, tenantIds.get(0));
        } else if (tenantIds.size() > 1) {
            return conditionIn(alias, tenantIds);
        } else {
            return null;
        }
    }

    private List<Object> getTenantId(Object paramTenantId) {
        List<Object> tenantIds = null;
        if (paramTenantId != null) {
            tenantIds = new ArrayList<>();
            if (paramTenantId instanceof Collection) {
                tenantIds.addAll((Collection<?>) paramTenantId);
            } else {
                tenantIds.add(paramTenantId);
            }
        }
        if (tenantIds == null) {
            tenantIds = this.tenantInfoHandler.getTenantIds();
            if (tenantIds == null) {
                tenantIds = new ArrayList<>();
            }
        }
        return tenantIds;
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
        if (where == null) {
            return false;
        }
        // 精确匹配租户ID列名，避免 tenant_id_flag 这类列名被误判为已有租户条件
        if (isTenantIdColumnExpr(where)) {
            return true;
        }
        if (where instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) where;
            return isContainsTenantIdCondition(binaryOpExpr.getLeft())
                    || isContainsTenantIdCondition(binaryOpExpr.getRight());
        }
        if (where instanceof SQLInListExpr) {
            return isTenantIdColumnExpr(((SQLInListExpr) where).getExpr());
        }
        if (where instanceof SQLInSubQueryExpr) {
            return isTenantIdColumnExpr(((SQLInSubQueryExpr) where).getExpr());
        }
        if (where instanceof SQLNotExpr) {
            return isContainsTenantIdCondition(((SQLNotExpr) where).getExpr());
        }
        return false;
    }

    /**
     * 判断表达式是否为租户ID字段列
     *
     * @param sqlExpr 列表达式
     * @return 是否为租户ID字段列
     */
    private boolean isTenantIdColumnExpr(SQLExpr sqlExpr) {
        String tenantIdColumn = this.tenantInfoHandler.getTenantIdColumn();
        if (tenantIdColumn == null || sqlExpr == null) {
            return false;
        }
        String columnName = null;
        if (sqlExpr instanceof SQLIdentifierExpr) {
            columnName = ((SQLIdentifierExpr) sqlExpr).getName();
        } else if (sqlExpr instanceof SQLPropertyExpr) {
            columnName = ((SQLPropertyExpr) sqlExpr).getName();
        }
        if (columnName == null) {
            return false;
        }
        return tenantIdColumn.equalsIgnoreCase(removeIdentifierQuote(columnName));
    }

    /**
     * 去掉列名上的引用符号，例如 `tenant_id`、"tenant_id"、[tenant_id]
     *
     * @param name 列名
     * @return 去掉引用符号后的列名
     */
    private static String removeIdentifierQuote(String name) {
        if (name == null) {
            return null;
        }
        String result = name.trim();
        if (result.length() >= 2) {
            if ((result.startsWith("`") && result.endsWith("`"))
                    || (result.startsWith("\"") && result.endsWith("\""))
                    || (result.startsWith("[") && result.endsWith("]"))) {
                result = result.substring(1, result.length() - 1);
            }
        }
        return result;
    }

    /**
     * union 查询
     *
     * @param sqlUnionQuery
     */
    private void unionQuery(SQLUnionQuery sqlUnionQuery, Object paramTenantId) {
        sqlUnionQuery.getRelations().forEach(sqlSelectQuery -> processSelectBody(sqlSelectQuery, paramTenantId));
    }

    /**
     * insert select 语法插入，例如 INSERT INTO role SELECT * FROM role_test
     *
     * @param insert         insert 对象
     * @param tenantIdColumn 租户ID字段
     * @param paramTenantId  mapper 接口租户ID
     */
    private void handlerInsertSelect(SQLInsertStatement insert, String tenantIdColumn, Object paramTenantId) {
        // 处理 insert select用法
        SQLSelect query = insert.getQuery();
        if (query != null) {
            SQLSelectQuery sqlSelectQuery = query.getQuery();
            // 处理 SELECT 语句条件
            processSelectBody(sqlSelectQuery, paramTenantId);
            // 处理查询字段
            if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock block = (SQLSelectQueryBlock) sqlSelectQuery;

                boolean isExistTenantIdColumn = false;
                for (SQLSelectItem sqlSelectItem : block.getSelectList()) {
                    String expr = sqlSelectItem.getExpr().toString();
                    // 如果 insert 中 select 语句结果集包含租户ID字段或者 结果集为 * 则不需要添加租户ID字段
                    if (expr.contains(tenantIdColumn) || "*".equals(expr)) {
                        isExistTenantIdColumn = true;
                        break;
                    }
                }

                if (!isExistTenantIdColumn) {
                    SQLSelectItem selectItem = block.findSelectItem(tenantIdColumn);
                    // 查询字段不包括租户,需要添加上
                    if (selectItem == null) {
                        block.addSelectItem(tenantIdColumn, null);
                    }
                }
            }
        }
    }

    /**
     * SQL语句 存在 join 查找最后的where条件的主表，例如 DELETE FROM test_table sp LEFT JOIN test_table1 re ON sp.id=re.data_id 查找 test_table 表
     *
     * @param joinTableSource join语句
     * @return
     */
    private SQLExprTableSource lastJoinTableSourceLeadTable(SQLJoinTableSource joinTableSource) {
        SQLExprTableSource sqlExprTableSource = null;
        while (true) {
            if (joinTableSource.getLeft() instanceof SQLExprTableSource) {
                sqlExprTableSource = (SQLExprTableSource) joinTableSource.getLeft();
                break;
            } else if (joinTableSource.getLeft() instanceof SQLJoinTableSource) {
                joinTableSource = (SQLJoinTableSource) joinTableSource.getLeft();
            } else {
                break;
            }
        }
        return sqlExprTableSource;
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
        // CTE(with 子句) 名称不是真实表，当前作用域内引用它时不需要拼接租户条件
        Set<String> cteAliases = CTE_ALIASES.get();
        if (cteAliases != null && cteAliases.contains(toCteKey(tableName))) {
            return true;
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
            if (tableName.startsWith(tableNamePrefix)) {
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

        if (!isEmpty(ignoreMatchTableAlias)) {
            for (String ignoreTableAlias : ignoreMatchTableAlias) {
                if (tableAlias.startsWith(ignoreTableAlias)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断数据源是否直接跳过租户ID设置
     *
     * @return
     */
    public boolean isIgnoreDynamicDatasource() {
        String ignoreDynamicDatasource = tenantService.ignoreDynamicDatasource();
        List<String> dynamicDatasourceList = tenantInfoHandler.ignoreDynamicDatasource();
        if (ignoreDynamicDatasource == null || ignoreDynamicDatasource.length() == 0 || isEmpty(dynamicDatasourceList)) {
            return false;
        }
        for (String ignoreDynamicDatasourceItem : dynamicDatasourceList) {
            if (ignoreDynamicDatasource.equals(ignoreDynamicDatasourceItem)) {
                return true;
            }
        }
        return false;
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

    public TenantInfoHandler getTenantInfoHandler() {
        return this.tenantInfoHandler;
    }

    public void setTenantService(ITenantService tenantService) {
        this.tenantService = tenantService;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public boolean isSkipParser() {
        return tenantService.skipParser();
    }

    public void threadLocalSkipParserSet() {
        tenantService.threadLocalSkipParserSet();
    }

    public void threadLocalSkipParserClear() {
        tenantService.threadLocalSkipParserClear();
    }
}
