package com.github.osinn.druid.multi.tenant.plugin.filter;

import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Druid 的过滤器
 *
 * @author KisChang
 */
@Component
public class DruidTenantFilter extends FilterAdapter implements BeanFactoryPostProcessor {

    private static ConfigurableListableBeanFactory beanFactory; // Spring应用上下文环境
    private static final DefaultSqlParser DEFAULT_SQL_PARSER = new DefaultSqlParser();

    public DruidTenantFilter() {
    }

    @Override
    public void init(DataSourceProxy dataSource) {
        super.init(dataSource);
        DEFAULT_SQL_PARSER.setTenantService(beanFactory.getBean(ITenantService.class));
        DEFAULT_SQL_PARSER.setTenantInfoHandler(beanFactory.getBean(TenantInfoHandler.class));
    }

    /* 这部分以DDL 为主
    @Override
    public boolean statement_execute(
            FilterChain chain,
            com.alibaba.druid.proxy.jdbc.StatementProxy statement,
            String sql) throws SQLException {
        return chain.statement_execute(statement, rewriteSql(sql));
    }*/

    @Override
    public PreparedStatementProxy connection_prepareStatement(
            FilterChain chain,
            ConnectionProxy connection,
            String sql) throws SQLException {
        String newSql;
        if (DEFAULT_SQL_PARSER.isSkipParser()) {
            newSql = sql;
        } else if (DEFAULT_SQL_PARSER.isIgnoreDynamicDatasource()) {
            newSql = sql;
        } else {
            // 处理 SQL
            String url = connection.getRawObject().getMetaData().getURL();
            Object paramTenantId = DEFAULT_SQL_PARSER.getTenantInfoHandler().getTenantIds();
            newSql = DEFAULT_SQL_PARSER.setTenantParameter(url, sql, paramTenantId);
        }
        // 使用修改后的 SQL
        return chain.connection_prepareStatement(connection, newSql);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        beanFactory = configurableListableBeanFactory;
    }
}
