package com.github.osinn.druid.multi.tenant.plugin.service;

import java.util.List;

/**
 * 提供外部实现接口以获取系统多租户ID服务接口
 *
 * @author wency_cai
 */
public interface ITenantService<T> {


    /**
     * 多租户ID可有一个或多个, 数据类型：Number或String
     *
     * @return 返回租户ID集合
     */
    List<T> getTenantIds();

    /**
     * 多数据源-获取外部当前执行的数据源，默认所有数据源都需要设置租户ID
     *
     * @return 返回当前执行的数据源，不为空则根据配置忽略的数据源跳过设置租户ID
     */
    default String ignoreDynamicDatasource() {
        return null;
    }
}
