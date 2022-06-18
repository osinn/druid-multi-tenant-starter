package com.github.osinn.druid.multi.tenant.plugin.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户参数配置
 *
 * @author wency_cai
 */
@Getter
@Setter
@ConfigurationProperties(prefix = TenantProperties.PREFIX)
public class TenantProperties {

    public final static String PREFIX = "mybatis.tenant.config";

    /**
     * 数据库中租户ID的列名
     */
    private String tenantIdColumn = "tenant_id";

    /**
     * 是否忽略表按租户ID过滤,默认所有表都按租户ID过滤，指定表名称或表前缀
     */
    private List<String> ignoreTablePrefix = new ArrayList<>();

    /**
     * 是否开启多租户配置,需要禁用druidFilterEnable
     */
    private boolean mybatisEnable;

    /**
     * 是否使用druid过滤器方式修改sql,需要禁用mybatisEnable
     */
    private boolean druidFilterEnable;

}
