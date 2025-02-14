package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
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
public class MybatisMultiTenantAutoConfigure {

    @Bean
    public MyBeanPostProcessor myBeanPostProcessor(ITenantService tenantService, TenantProperties tenantProperties) {
        return new MyBeanPostProcessor(tenantService, tenantProperties);
    }
}
