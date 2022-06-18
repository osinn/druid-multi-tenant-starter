package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.github.osinn.druid.multi.tenant.plugin.annotation.IgnoreTenantIdField;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;

/**
 * 处理需要忽略的Mapper
 *
 * @author wency_cai
 */
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MapperFactoryBean) {
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

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
