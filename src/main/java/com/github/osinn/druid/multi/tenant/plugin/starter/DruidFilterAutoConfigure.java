package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.pool.DruidDataSource;
import com.github.osinn.druid.multi.tenant.plugin.filter.RewriteSqlDruidFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * 描述
 *
 * @author wency_cai
 */
@Configuration
@ConditionalOnProperty(value = TenantProperties.PREFIX + ".druid-filter-enable", havingValue = "true")
public class DruidFilterAutoConfigure {

    @ConfigurationProperties("spring.datasource")
    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setProxyFilters(Collections.singletonList(rewriteSqlDruidFilter()));
        return dataSource;
    }

    @Bean
    public FilterAdapter rewriteSqlDruidFilter() {
        return new RewriteSqlDruidFilter();
    }
}
