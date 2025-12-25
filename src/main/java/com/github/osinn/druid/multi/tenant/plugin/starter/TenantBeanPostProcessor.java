package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.support.ITenantSqlInterceptorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;

/**
 * 处理Bean
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
        if (tenantProperties.isEnableDynamicDatasource() && bean instanceof DataSource) {
            // 调用外部添加数据源代理 SQL 解析器逻辑，如果返回空，简单判断一下是否存在 baomidou 的多数据源，如果存在则添加一个过滤器
            Object object = tenantService.addDataSourceProxySqlParser(bean, defaultSqlParser);
            if (object == null) {
                return bean;
            } else {
                return object;
            }
        }
        return bean;
    }
}
