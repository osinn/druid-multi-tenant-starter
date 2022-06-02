package com.github.osinn.druid.multi.tenant.plugin.filter;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

import java.sql.SQLException;

/**
 * 基于druid连接池过滤器修改sql
 * <p>
 *     @ConfigurationProperties("spring.datasource")
 *     @Bean(initMethod = "init", destroyMethod = "close")
 *     public DruidDataSource dataSource(){
 *         DruidDataSource dataSource = new DruidDataSource();
 *         dataSource.setProxyFilters(Collections.singletonList(rewriteSqlDruidFilter()));
 *         return dataSource;
 *     }
 *     @Bean
 *     public FilterAdapter rewriteSqlDruidFilter() {
 *         return new RewriteSqlDruidFilter();
 *     }
 * </p>
 *
 * @author wency_cai
 */
public class RewriteSqlDruidFilter extends FilterEventAdapter {

    private static final DefaultSqlParser DEFAULT_SQL_PARSER = new DefaultSqlParser();

    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql) throws SQLException {
        sql = DEFAULT_SQL_PARSER.setTenantParameter(sql);
        return super.connection_prepareStatement(chain, connection, sql);
    }
}
