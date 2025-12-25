package com.github.osinn.druid.multi.tenant.plugin.support.jpa;

import com.github.osinn.druid.multi.tenant.plugin.support.ITenantSqlInterceptorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * JPA 拦截器实现方式
 *
 * @author wency_cai
 */
@Slf4j
public class JpaTenantSqlInterceptorStrategy implements ITenantSqlInterceptorStrategy {

    @Override
    public Object doHandle(Object bean, String beanName) {
        if (bean instanceof LocalContainerEntityManagerFactoryBean) {
            log.debug("添加自定义 JPA 多租户SQL拦截器");
            LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = (LocalContainerEntityManagerFactoryBean) bean;
            //注入拦截器
            localContainerEntityManagerFactoryBean.getJpaPropertyMap().put(
                    "hibernate.session_factory.statement_inspector",
                    JpaTenantInterceptor.class.getName());
        }

        return bean;
    }
}
