package com.github.osinn.druid.multi.tenant.plugin.service;

import java.util.List;

/**
 * 提供外部实现接口以获取系统多租户ID服务接口
 *
 * @author wency_cai
 */
public interface ITenantService {


    /**
     * 多租户ID可有一个或多个, 数据类型：Number或String
     *
     * @return 返回租户ID集合
     */
    List<Object> getTenantIds();

    /**
     * 多数据源-获取外部当前执行的数据源，默认所有数据源都需要设置租户ID
     *
     * @return 返回当前执行的数据源，不为空则根据配置忽略的数据源跳过设置租户ID
     */
    default String ignoreDynamicDatasource() {
        return null;
    }

    /**
     * 自定义解析
     * 此方法若有返回值，意味着不会执行默认解析、before、after方法也不会调用
     * 可以根据业务场景自行解析，如果返回 null 则依然执行默认的解析，否则认为开发者已经自行解析设置过租户ID，使用返回的SQL语句
     *
     * @param originalSQL   原始SQL
     * @param paramTenantId Mapper接口参数租户ID
     * @return 返回解析后的SQL
     */
    default String customizeParser(String originalSQL, Object paramTenantId) {
        return null;
    }

    /**
     * 执行顺序 customizeParser -> before
     * 执行默认的解析前调用
     *
     * @param originalSQL   原始SQL
     * @param paramTenantId Mapper接口参数租户ID
     * @return 返回解析后的SQL
     */
    default String before(String originalSQL, Object paramTenantId) {
        return null;
    }

    /**
     * 执行顺序 before -> after
     * 执行默认的解析完成后调用
     *
     * @param newSQL        解析后的SQL
     * @param paramTenantId Mapper接口参数租户ID
     * @return 返回解析后的SQL
     */
    default String after(String newSQL, Object paramTenantId) {
        return null;
    }

    /**
     * 是否跳过解析，可以重写此方法，(实现当前线程全局判断是否忽略tenant_id字段)
     * 此方法可以用在此场景，如：init()方法下执行的所有sql语句忽略添加tenant_id字段，可以自行实现 ThreadLocal
     * 并在此方法中获取当前线程变量判断是否需要忽略添加tenant_id字段，如果需要忽略添加tenant_id字段，则返回true即可
     *
     * @return 返回 true 跳过解析, false 继续解析
     */
    default boolean skipParser() {
        return false;
    }
}
