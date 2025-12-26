package com.github.osinn.druid.multi.tenant.plugin.filter;

import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.starter.TenantProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.sql.SQLException;
import java.util.List;

/**
 * 通过 BeanPostProcessor 给Druid添加一个自定义的过滤器 DruidTenantFilter
 *
 * @author KisChang
 * @see com.github.osinn.druid.multi.tenant.plugin.filter.DruidTenantFilter
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnProperty(name = "mybatis.tenant.config.enable", havingValue = "true")
public class DruidTenantProcessor implements BeanPostProcessor {

    @Bean
    public TenantInfoHandler bean(ITenantService tenantService, TenantProperties tenantProperties) {
        return new TenantInfoHandler() {
            public List<Object> getTenantIds() {
                return tenantService.getTenantIds();
            }

            public List<String> ignoreTableName() {
                return tenantProperties.getIgnoreTableName();
            }

            public List<String> ignoreMatchTableAlias() {
                return tenantProperties.getIgnoreMatchTableAlias();
            }

            public List<String> ignoreTableNamePrefix() {
                return tenantProperties.getIgnoreTableNamePrefix();
            }

            public String getTenantIdColumn() {
                return tenantProperties.getTenantIdColumn();
            }

            public List<String> ignoreDynamicDatasource() {
                return tenantProperties.getIgnoreDynamicDatasource();
            }

            public DbType getDbType() {
                return tenantProperties.getDbType();
            }
        };
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DruidDataSource) {
            try {
                ((DruidDataSource) bean).addFilters(DruidTenantFilter.class.getTypeName());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

}
