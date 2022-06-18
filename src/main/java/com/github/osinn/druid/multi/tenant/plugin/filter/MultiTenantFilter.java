package com.github.osinn.druid.multi.tenant.plugin.filter;

/**
 * 过滤器接口
 *
 * @author wency_cai
 **/
public interface MultiTenantFilter {

    /**
     * 默认过滤还是忽略
     *
     * @param table 表名称
     * @return true表示按租户ID过滤
     */
    boolean doTableFilter(String table);
}
