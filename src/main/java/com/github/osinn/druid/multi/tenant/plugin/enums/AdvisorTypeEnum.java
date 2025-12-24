package com.github.osinn.druid.multi.tenant.plugin.enums;

/**
 * 描述
 *
 * @author wency_cai
 */
public enum AdvisorTypeEnum {

    /**
     * 使用 JPA 拦截器
     */
    jpa,
    /**
     * 使用 Mybatis 拦截器
     */
    mybatis,
    /**
     * 使用 druid 过滤器，前提是使用的数据源必须是 DruidDataSource 数据
     */
    druid
}
