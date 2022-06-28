package com.github.osinn.druid.multi.tenant.plugin.handler;

import com.alibaba.druid.DbType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 描述
 *
 * @author wency_cai
 */
public interface TenantInfoHandler {

    /**
     * 数据库方言
     */
    DbType getDbType();

    /**
     * 存储需要忽略的Mapper方法
     */
    Set<String> IGNORE_TENANT_ID_METHODS = new HashSet<>();

    /**
     * 多租户id, Number类型或String类型
     *
     * @return 返回多租户id集合，一个或多个
     */
    <T> List<T> getTenantIds();

    /**
     * 根据表名判断是否忽略拼接多租户ID条件
     * 忽略 sys_ 开头的表
     *
     * @return 返回 忽略的表名称或表前缀
     */
    List<String> ignoreTableName();

    /**
     * 获取租户字段名
     * <p>
     * 默认字段名叫: tenant_id
     *
     * @return 租户字段名
     */
    String getTenantIdColumn();
}
