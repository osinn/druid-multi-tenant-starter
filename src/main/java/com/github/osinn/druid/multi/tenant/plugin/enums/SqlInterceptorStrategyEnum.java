package com.github.osinn.druid.multi.tenant.plugin.enums;

/**
 * SQL 拦截类型
 *
 * @author wency_cai
 */
public enum SqlInterceptorStrategyEnum {

    /**
     * 使用 JPA 拦截器, 如果ORM框架使用的是 JPA 框架且数据源使用的是DruidDataSource
     */
    jpa_interceptor,

    /**
     * 使用 Mybatis 拦截器
     */
    mybatis_interceptor,

    /**
     * 使用 druid 过滤器，前提是使用的数据源必须是 DruidDataSource 数据
     * <p>
     * 如果项目中使用到 baomidou dynamic-datasource 多动态数据源 或 使用了 spring-cloud-starter-sleuth 且配置为
     * <pre>
     * spring:
     *   sleuth:
     *     jdbc:
     *       enabled: true
     * </pre>
     * 需要 yml 配置 data-source-wrapper-type 指定类型，否则无法将 druid 过滤器 SQL 解析器添加到 DruidDataSource 数据源中
     */
    druid_filter
}
