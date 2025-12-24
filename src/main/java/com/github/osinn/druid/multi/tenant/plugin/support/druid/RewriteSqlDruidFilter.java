package com.github.osinn.druid.multi.tenant.plugin.support.druid;


import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.starter.TenantProperties;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;

/**
 * 基于 druid 数据源过滤器修改sql
 *
 * @author wency_cai
 */
@Slf4j
public class RewriteSqlDruidFilter extends FilterEventAdapter {

    private final DefaultSqlParser defaultSqlParser;

    public RewriteSqlDruidFilter(ITenantService tenantService, TenantProperties tenantProperties) {
        this.defaultSqlParser = getDefaultSqlParser(tenantService, tenantProperties);
    }


    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql) throws SQLException {
        if (defaultSqlParser.isSkipParser()) {
            return super.connection_prepareStatement(chain, connection, sql);
        } else {
            String url = connection.getRawObject().getMetaData().getURL();
            sql = defaultSqlParser.setTenantParameter(url, sql, null);
            log.debug("最终执行SQL ===========>\n {} \n<===========", sql);
            return super.connection_prepareStatement(chain, connection, sql);
        }
    }


    private DefaultSqlParser getDefaultSqlParser(ITenantService tenantService, TenantProperties tenantProperties) {
        DefaultSqlParser defaultSqlParser = new DefaultSqlParser();

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
        return defaultSqlParser;
    }
}