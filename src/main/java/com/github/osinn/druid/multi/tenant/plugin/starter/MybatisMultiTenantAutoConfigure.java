package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * 多租户插件自动配置
 *
 * @author wency_cai
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(value = TenantProperties.PREFIX + ".mybatis-enable", havingValue = "true")
public class MybatisMultiTenantAutoConfigure {

    @Bean
    public StartSysListener startSysListener(ITenantService tenantService, TenantProperties tenantProperties, List<SqlSessionFactory> sqlSessionFactoryList) {
        return new StartSysListener(tenantService, tenantProperties, sqlSessionFactoryList);
    }

    @Bean
    public MyBeanPostProcessor myBeanPostProcessor() {
        return new MyBeanPostProcessor();
    }
}
