package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.support.BaseOrmTenantAdvisor;
import com.github.osinn.druid.multi.tenant.plugin.support.IgnoreTenantIdFieldPointcutAdvisor;
import com.github.osinn.druid.multi.tenant.plugin.support.druid.DruidTenantAdvisor;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.IJpaEntityManagerFactory;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.JpaEntityManagerFactoryBoot2;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.JpaEntityManagerFactoryBoot3;
import com.github.osinn.druid.multi.tenant.plugin.support.jpa.JpaOrmTenantAdvisor;
import com.github.osinn.druid.multi.tenant.plugin.support.mybatis.MybatisOrmTenantAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnClass(name = "javax.persistence.EntityManager")
    @ConditionalOnMissingBean
    public IJpaEntityManagerFactory jpaEntityManagerFactoryBoot2() {
        return new JpaEntityManagerFactoryBoot2();
    }

    // Spring Boot 3+: 存在 jakarta.persistence.EntityManager
    @Bean
    @ConditionalOnClass(name = "jakarta.persistence.EntityManager")
    @ConditionalOnMissingBean
    public IJpaEntityManagerFactory jpaEntityManagerFactoryBoot3() {
        return new JpaEntityManagerFactoryBoot3();
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".advisor-type", havingValue = "jpa")
    public BaseOrmTenantAdvisor jpaOrmTenantAdvisor() {
        return new JpaOrmTenantAdvisor();
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".advisor-type", havingValue = "mybatis")
    public BaseOrmTenantAdvisor mybatisOrmTenantAdvisor(ITenantService tenantService, TenantProperties tenantProperties) {
        return new MybatisOrmTenantAdvisor(tenantService, tenantProperties);
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".advisor-type", havingValue = "druid")
    public BaseOrmTenantAdvisor druidTenantAdvisor(ITenantService tenantService, TenantProperties tenantProperties) {
        return new DruidTenantAdvisor(tenantService, tenantProperties);
    }

    @Bean
    @ConditionalOnProperty(value = TenantProperties.PREFIX + ".enable-pointcut-advisor", havingValue = "true")
    public IgnoreTenantIdFieldPointcutAdvisor ignoreTenantIdFieldPointcutAdvisor() {
        return new IgnoreTenantIdFieldPointcutAdvisor();
    }

    @Bean
    public TenantBeanPostProcessor myBeanPostProcessor(BaseOrmTenantAdvisor baseOrmTenantAdvisor, ITenantService tenantService) {
        return new TenantBeanPostProcessor(baseOrmTenantAdvisor);
    }
}
