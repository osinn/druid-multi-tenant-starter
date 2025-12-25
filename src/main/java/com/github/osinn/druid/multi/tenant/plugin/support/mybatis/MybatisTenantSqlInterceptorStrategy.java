package com.github.osinn.druid.multi.tenant.plugin.support.mybatis;

import com.github.osinn.druid.multi.tenant.plugin.annotation.IgnoreTenantIdField;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.starter.TenantProperties;
import com.github.osinn.druid.multi.tenant.plugin.support.ITenantSqlInterceptorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;

import java.lang.reflect.Method;

/**
 * MyBatis 拦截器实现方式
 *
 * @author wency_cai
 */
@Slf4j
public class MybatisTenantSqlInterceptorStrategy implements ITenantSqlInterceptorStrategy {


    private final DefaultSqlParser defaultSqlParser;
    private final TenantProperties tenantProperties;

    public MybatisTenantSqlInterceptorStrategy(DefaultSqlParser defaultSqlParser, TenantProperties tenantProperties) {
        this.defaultSqlParser = defaultSqlParser;
        this.tenantProperties = tenantProperties;
    }

    @Override
    public Object doHandle(Object bean, String beanName) {
        if (bean instanceof SqlSessionFactory) {
            log.debug("添加自定义Mybatis多租户SQL拦截器");
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
            // 添加拦截器
            sqlSessionFactory.getConfiguration().addInterceptor(new MybatisMultiTenantPluginInterceptor(defaultSqlParser));
        }
        if (bean instanceof MapperFactoryBean && !tenantProperties.isEnablePointcutAdvisorIgnoreTenantId()) {
            MapperFactoryBean mapperFactoryBean = (MapperFactoryBean) bean;
            final String mapperName = mapperFactoryBean.getObjectType().getName();
            Method[] methods = mapperFactoryBean.getObjectType().getMethods();
            for (Method item : methods) {
                IgnoreTenantIdField annotation = item.getAnnotation(IgnoreTenantIdField.class);
                if (annotation != null) {
                    TenantInfoHandler.IGNORE_TENANT_ID_METHODS.add(String.format("%s.%s", mapperName, item.getName()));
                }
            }
        }
        return bean;
    }
}
