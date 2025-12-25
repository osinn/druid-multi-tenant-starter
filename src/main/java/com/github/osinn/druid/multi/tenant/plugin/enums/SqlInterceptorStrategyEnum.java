package com.github.osinn.druid.multi.tenant.plugin.enums;

/**
 * SQL 拦截类型
 *
 * @author wency_cai
 */
public enum SqlInterceptorStrategyEnum {

    /**
     * 使用 JPA 拦截器, 如果ORM框架使用的是 JPA 框架且数据源使用的是DruidDataSource，推荐使用 druid_filter 过滤器
     */
    jpa_interceptor,
    /**
     * 使用 Mybatis 拦截器
     */
    mybatis_interceptor,
    /**
     * 使用 druid 过滤器，前提是使用的数据源必须是 DruidDataSource 数据
     */
    druid_filter
}
