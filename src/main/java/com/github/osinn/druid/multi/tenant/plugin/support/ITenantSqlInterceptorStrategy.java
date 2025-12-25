package com.github.osinn.druid.multi.tenant.plugin.support;

/**
 * 租户 SQL 拦截策略接口
 *
 * @author wency_cai
 */
public interface ITenantSqlInterceptorStrategy {

    Object doHandle(Object bean, String beanName);
}
