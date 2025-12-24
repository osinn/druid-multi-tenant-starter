package com.github.osinn.druid.multi.tenant.plugin.support.jpa;

import com.alibaba.druid.DbType;
import com.github.osinn.druid.multi.tenant.plugin.context.TenantApplicationContext;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.starter.TenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.List;

/**
 * JPA 拦截器
 *
 * @author wency_cai
 */
@Slf4j
public class JpaTenantInterceptor implements StatementInspector {

    private DefaultSqlParser defaultSqlParser = new DefaultSqlParser();

    private IJpaEntityManagerFactory jpaEntityManagerFactory;

    public JpaTenantInterceptor() {
        initBean();
    }

    @Override
    public String inspect(String sql) {
        if (defaultSqlParser.isSkipParser()) {
            return sql;
        } else {
            String url = jpaEntityManagerFactory.getDataSourceUrl();
            sql = defaultSqlParser.setTenantParameter(url, sql, null);
            log.debug("最终执行SQL ===========>\n {} \n<===========", sql);
            return sql;
        }
    }

    private void initBean() {
        jpaEntityManagerFactory = TenantApplicationContext.getBean(IJpaEntityManagerFactory.class);


        ITenantService tenantService = TenantApplicationContext.getBean(ITenantService.class);
        TenantProperties tenantProperties = TenantApplicationContext.getBean(TenantProperties.class);

        defaultSqlParser.setTenantService(tenantService);
        defaultSqlParser.setTenantInfoHandler(new TenantInfoHandler() {

            @Override
            public List<Object> getTenantIds() {
                return tenantService.getTenantIds();
            }

            @Override
            public List<String> ignoreTableName() {
                return tenantProperties.getIgnoreTableName();
            }

            @Override
            public List<String> ignoreMatchTableAlias() {
                return tenantProperties.getIgnoreMatchTableAlias();
            }

            @Override
            public List<String> ignoreTableNamePrefix() {
                return tenantProperties.getIgnoreTableNamePrefix();
            }

            @Override
            public String getTenantIdColumn() {
                return tenantProperties.getTenantIdColumn();
            }

            @Override
            public List<String> ignoreDynamicDatasource() {
                return tenantProperties.getIgnoreDynamicDatasource();
            }

            @Override
            public DbType getDbType() {
                return tenantProperties.getDbType();
            }

        });
    }
}