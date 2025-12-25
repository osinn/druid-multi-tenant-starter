package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.alibaba.druid.DbType;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.support.ITenantSqlInterceptorStrategy;
import com.github.osinn.druid.multi.tenant.plugin.support.IgnoreTenantIdFieldPointcutAdvisor;
import com.github.osinn.druid.multi.tenant.plugin.support.TenantDataSourceProxy;
import com.github.osinn.druid.multi.tenant.plugin.support.druid.DruidFilterTenantSqlInterceptorStrategy;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.IJpaEntityManagerFactory;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.JpaEntityManagerFactoryBoot2;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.JpaEntityManagerFactoryBoot3;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.JpaTenantSqlInterceptorStrategy;
import com.github.osinn.druid.multi.tenant.plugin.support.mybatis.MybatisTenantSqlInterceptorStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

/**
 * 多租户插件自动配置
 *
 * @author wency_cai
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(value = TenantProperties.PREFIX + ".enable", havingValue = "true")
public class MultiTenantAutoConfigure {

    // Spring Boot 2: 存在 javax.persistence.EntityManager
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "javax.persistence.EntityManager")
    public IJpaEntityManagerFactory jpaEntityManagerFactoryBoot2() {
        return new JpaEntityManagerFactoryBoot2();
    }

    // Spring Boot 3+: 存在 jakarta.persistence.EntityManager
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "jakarta.persistence.EntityManager")
    public IJpaEntityManagerFactory jpaEntityManagerFactoryBoot3() {
        return new JpaEntityManagerFactoryBoot3();
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".sql-interceptor-strategy", havingValue = "jpa_interceptor")
    public ITenantSqlInterceptorStrategy jpaTenantSqlInterceptorStrategy() {
        return new JpaTenantSqlInterceptorStrategy();
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".sql-interceptor-strategy", havingValue = "mybatis_interceptor")
    public ITenantSqlInterceptorStrategy mybatisTenantSqlInterceptorStrategy(DefaultSqlParser defaultSqlParser, TenantProperties tenantProperties) {
        return new MybatisTenantSqlInterceptorStrategy(defaultSqlParser, tenantProperties);
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".sql-interceptor-strategy", havingValue = "druid_filter")
    public ITenantSqlInterceptorStrategy druidFilterTenantSqlInterceptorStrategy(DefaultSqlParser defaultSqlParser) {
        return new DruidFilterTenantSqlInterceptorStrategy(defaultSqlParser);
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".enable-pointcut-advisor-ignore-tenant-id", havingValue = "true")
    public IgnoreTenantIdFieldPointcutAdvisor ignoreTenantIdFieldPointcutAdvisor(DefaultSqlParser defaultSqlParser) {
        return new IgnoreTenantIdFieldPointcutAdvisor(defaultSqlParser);
    }

    @Bean
    public DefaultSqlParser defaultSqlParser(ITenantService tenantService, TenantProperties tenantProperties) {
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

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".enable-pointcut-advisor-ignore-tenant-id", havingValue = "true")
    public TenantDataSourceProxy tenantDataSourceProxy(DefaultSqlParser defaultSqlParser, DataSource dataSource) {
        return new TenantDataSourceProxy(defaultSqlParser, dataSource);
    }

    @Bean
    public TenantBeanPostProcessor myBeanPostProcessor(TenantProperties tenantPropertie, DefaultSqlParser defaultSqlParser, ITenantSqlInterceptorStrategy tenantSqlInterceptorStrategy, ITenantService tenantService) {
        return new TenantBeanPostProcessor(tenantPropertie, defaultSqlParser, tenantSqlInterceptorStrategy, tenantService);
    }
}
