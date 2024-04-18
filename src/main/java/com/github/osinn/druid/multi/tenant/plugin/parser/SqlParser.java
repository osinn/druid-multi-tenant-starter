package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.alibaba.druid.sql.ast.statement.*;

/**
 * sql解析器接口
 *
 * @author wency_cai
 */
public interface SqlParser {

    /**
     * sql语句处理入口
     *
     * @param sql 语句处理入口
     * @return 返回串改后的sql语句
     */
    String setTenantParameter(String sql);

    /**
     * sql语句处理入口
     *
     * @param sql 语句处理入口
     * @return 返回串改后的sql语句
     */
    String setTenantParameter(String sql, Object paramTenantId);

    /**
     * 解析查询语句
     *
     * @param selectBody select语句处理
     */
    void processSelectBody(SQLSelectQuery selectBody, Object paramTenantId);

    /**
     * 解析新增语句
     *
     * @param insert 语句处理
     */
    void processInsert(SQLInsertStatement insert, Object paramTenantId);

    /**
     * 解析更新语句
     *
     * @param update 语句处理
     */
    void processUpdate(SQLUpdateStatement update, Object paramTenantId);

    /**
     * 解析删除语句
     *
     * @param delete 删除语句处理
     */
    void processDelete(SQLDeleteStatement delete, Object paramTenantId);
}
