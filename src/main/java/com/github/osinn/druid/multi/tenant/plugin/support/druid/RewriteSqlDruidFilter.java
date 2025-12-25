package com.github.osinn.druid.multi.tenant.plugin.support.druid;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * 基于 druid 数据源过滤器修改sql
 *
 * @author wency_cai
 */
@Slf4j
public class RewriteSqlDruidFilter extends FilterEventAdapter {

    private final DefaultSqlParser defaultSqlParser;

    public RewriteSqlDruidFilter(DefaultSqlParser defaultSqlParser) {
        this.defaultSqlParser = defaultSqlParser;
    }


    @Override
    public PreparedStatementProxy connection_prepareStatement(FilterChain chain, ConnectionProxy connection, String sql) throws SQLException {
        if (!defaultSqlParser.isSkipParser()) {
            String url = connection.getRawObject().getMetaData().getURL();
            sql = defaultSqlParser.setTenantParameter(url, sql, null);
        }
        log.debug("最终执行SQL ===========>\n {} \n<===========", sql);
        return super.connection_prepareStatement(chain, connection, sql);
    }


}