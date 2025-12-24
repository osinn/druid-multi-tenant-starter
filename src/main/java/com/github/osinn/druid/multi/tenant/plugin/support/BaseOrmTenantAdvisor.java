package com.github.osinn.druid.multi.tenant.plugin.support;


/**
 * 描述
 *
 * @author wency_cai
 */
public interface BaseOrmTenantAdvisor {

    Object doHandle(Object bean, String beanName);
}
