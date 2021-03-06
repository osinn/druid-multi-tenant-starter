package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.util.JdbcConstants;
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
     * 是否忽略表按租户ID过滤,默认所有表都按租户ID过滤，指定表名称
     */
    private List<String> ignoreTableName = new ArrayList<>();

    /**
     * 是否使用MyBatis拦截器方式修改sql,需要禁用druidFilterEnable
     */
    private boolean enable;

    /**
     * 是否使用druid过滤器方式修改sql,需要禁用enable
     */
    private boolean druidFilterEnable;

    /**
     * 数据库方言
     */
    private DbType dbType = JdbcConstants.MYSQL;

}
