package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.support.DataSourceHelp;
import com.github.osinn.druid.multi.tenant.plugin.support.ITenantSqlInterceptorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;

/**
 * 处理 Bean
 *
 * @author wency_cai
 */
@Slf4j
public class TenantBeanPostProcessor implements BeanPostProcessor {

    private final ITenantSqlInterceptorStrategy tenantSqlInterceptorStrategy;
    private final ITenantService tenantService;
    private final DefaultSqlParser defaultSqlParser;
    private final TenantProperties tenantProperties;

    public TenantBeanPostProcessor(TenantProperties tenantProperties, DefaultSqlParser defaultSqlParser, ITenantSqlInterceptorStrategy tenantSqlInterceptorStrategy, ITenantService tenantService) {
        this.tenantSqlInterceptorStrategy = tenantSqlInterceptorStrategy;
        this.tenantService = tenantService;
        this.defaultSqlParser = defaultSqlParser;
        this.tenantProperties = tenantProperties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return this.tenantSqlInterceptorStrategy.doHandle(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            // 判断是否有代理数据源
            if (tenantProperties.getDataSourceWrapperType() != null) {
                DataSourceHelp.checkDataSource(bean, defaultSqlParser, tenantProperties);
            }
            if (tenantProperties.isEnableCustomDataSource()) {

                // 调用一下外部添加自定义添加数据源代理 SQL 解析器方法
                Object object = tenantService.addCustomDataSourceProxySqlParser(bean, defaultSqlParser);
                if (object == null) {
                    return bean;
                } else {
                    return object;
                }
            }
        }
        return bean;
    }

}
