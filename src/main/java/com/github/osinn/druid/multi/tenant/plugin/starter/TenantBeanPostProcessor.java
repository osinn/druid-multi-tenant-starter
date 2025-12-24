package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.github.osinn.druid.multi.tenant.plugin.support.BaseOrmTenantAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 处理Bean
 *
 * @author wency_cai
 */
@Slf4j
public class TenantBeanPostProcessor implements BeanPostProcessor {


    private final BaseOrmTenantAdvisor baseOrmTenantAdvisor;

    public TenantBeanPostProcessor(BaseOrmTenantAdvisor baseOrmTenantAdvisor) {
        this.baseOrmTenantAdvisor = baseOrmTenantAdvisor;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return this.baseOrmTenantAdvisor.doHandle(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
