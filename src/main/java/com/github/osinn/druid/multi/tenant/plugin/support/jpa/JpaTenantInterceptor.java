package com.github.osinn.druid.multi.tenant.plugin.support.jpa;

import com.github.osinn.druid.multi.tenant.plugin.context.TenantApplicationContext;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * JPA 拦截器
 *
 * @author wency_cai
 */
@Slf4j
public class JpaTenantInterceptor implements StatementInspector {

    private DefaultSqlParser defaultSqlParser;

    private IJpaEntityManagerFactory jpaEntityManagerFactory;

    public JpaTenantInterceptor() {
        initBean();
    }

    @Override
    public String inspect(String sql) {
        if (!defaultSqlParser.isSkipParser()) {
            String url = jpaEntityManagerFactory.getDataSourceUrl();
            sql = defaultSqlParser.setTenantParameter(url, sql, null);
        }
        log.debug("最终执行SQL ===========>\n {} \n<===========", sql);
        return sql;
    }

    private void initBean() {
        jpaEntityManagerFactory = TenantApplicationContext.getBean(IJpaEntityManagerFactory.class);
        defaultSqlParser = TenantApplicationContext.getBean(DefaultSqlParser.class);
    }
}