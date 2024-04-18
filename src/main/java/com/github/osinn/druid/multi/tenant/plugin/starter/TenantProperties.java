package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.util.JdbcConstants;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
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
     * 是否忽略表按租户ID过滤,默认所有表都按租户ID过滤，指定表名称(区分大小写全等判断)
     */
    private List<String> ignoreTableName = new ArrayList<>();

    /**
     * 匹配判断指定表别名是否忽略表按租户ID过滤(区分大小写匹配判断)
     */
    private List<String> ignoreMatchTableAlias = new ArrayList<>();

    /**
     * 忽略表名前缀
     */
    private List<String> ignoreTableNamePrefix = new ArrayList<>();

    /**
     * 忽略数据-多数据源情况下指定需要忽略的数据源
     * 需要重写 {@link ITenantService#ignoreDynamicDatasource} 方法，提供获取当前执行的数据源名称
     */
    private List<String> ignoreDynamicDatasource = new ArrayList<>();

    /**
     * 是否使用MyBatis拦截器方式修改sql
     */
    private boolean enable;

    /**
     * 数据库方言
     */
    private DbType dbType = JdbcConstants.MYSQL;

}
