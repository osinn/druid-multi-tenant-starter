package com.github.osinn.druid.multi.tenant.plugin.enums;

/**
 * 数据源包装代理类型
 *
 * @author wency_cai
 */

public enum DataSourceWrapperEnum {

    /**
     * 项目中存在 baomidou(苞米豆) 动态数据源
     */
    BAOMIDOU_DYNAMIC_DATA_SOURCE,

    /**
     * 项目中使用 sleuth 代理 jdbc 同时使用baomidou(苞米豆) 动态数据源
     * <pre>
     * 如果项目中使用到 sleuth 同时使用baomidou(苞米豆) 动态数据源，应该将 sleuth 中的 jdbc 关闭掉，例如以下配置，否则 sql-interceptor-strategy: druid_filter 是不会添加druid过滤器多租户 SQL 解析
     * spring:
     *   sleuth:
     *     jdbc:
     *       enabled: false
     * </pre>
     */
    SLEUTH_BAOMIDOU_DYNAMIC_DATA_SOURCE_WRAPPER
}
