package com.github.osinn.druid.multi.tenant.plugin.service;

import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

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

    /**
     * 只有 yml 配置 enable-pointcut-advisor = true 时 内部会使用到 ThreadLocal
     * 流程: 调用 threadLocalSkipParserSet 方法 设置是否跳过解析 ——> skipParser 从 ThreadLocal 中获取设置值判断 ——> threadLocalSkipParserClear 清理上下文
     * 可以重写此方法实现使用自定义 ThreadLocal 实现，对应 调用 ThreadLocal 的 set 方法，即为 myThreadLocal.set(xxxx)
     * 如果不实现此方法，会调用内部默认的 ThreadLocal 实现
     * <p>
     * 需要注意的是，内部使用的 ThreadLocal 不支持跨线程调用(即 方法上使用@IgnoreTenantIdField 注解，此方法内又有异步方法，在异步方法内操作数据库，此时不会生效，依然会解析设置租户ID)
     * 如果需要支持跨线程(异步方法)，你的框架可参考使用 TransmittableThreadLocal 库创建线程池，然后实现 skipParser、threadLocalSkipParserSet、threadLocalSkipParserClear这三个方法逻辑
     *
     * @since 1.5.6
     */
    default void threadLocalSkipParserSet() {

    }

    /**
     * 只有 yml 配置 enable-pointcut-advisor = true 时 内部会使用到 ThreadLocal
     * 可以重写此方法实现使用自定义 ThreadLocal 实现，对应 调用 ThreadLocal 的 clear 方法，即为 myThreadLocal.clear()
     * 如果不实现此方法，会调用内部默认的 ThreadLocal 实现
     *
     * @since 1.5.6
     */
    default void threadLocalSkipParserClear() {

    }

    /**
     * 预留方法，当bean初始化后调用，数据源添加代理 sql 解析器
     * yml 配置 enable-dynamic-datasource = true 时 才会调用此方法
     * <pre>
     * 如果项目中使用到 sleuth，应该将 sleuth 中的 jdbc 关闭掉，因为 sleuth 会将 dataSource 包装成 DataSourceWrapper 代理类
     * spring:
     *   sleuth:
     *     jdbc:
     *       enabled: false
     * </pre>
     *
     * @param dataSource       数据源
     * @param defaultSqlParser sql 解析器
     * @return 返回 dataSource 数据源, 如果返回 null 则使用内部的简单实现逻辑，并且需要引入 baomidou 的dynamic-datasource多数据源库
     * 内部只支持 baomidou 数据源 <a href="https://github.com/baomidou/dynamic-datasource">
     * @since 1.5.6
     */
    default Object addDataSourceProxySqlParser(Object dataSource, DefaultSqlParser defaultSqlParser) {

        return null;
    }

}
