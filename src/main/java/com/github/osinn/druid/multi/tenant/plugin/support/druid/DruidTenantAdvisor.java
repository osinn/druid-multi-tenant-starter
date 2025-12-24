package com.github.osinn.druid.multi.tenant.plugin.support.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.support.BaseOrmTenantAdvisor;
import com.github.osinn.druid.multi.tenant.plugin.starter.TenantProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 *
 * DruidDataSource 过滤器实现方式
 *
 * @author wency_cai
 */
@Slf4j
public class DruidTenantAdvisor implements BaseOrmTenantAdvisor {

    private final ITenantService tenantService;

    private final TenantProperties tenantProperties;

    public DruidTenantAdvisor(ITenantService tenantService, TenantProperties tenantProperties) {
        this.tenantService = tenantService;
        this.tenantProperties = tenantProperties;
    }

    @Override
    public Object doHandle(Object bean, String beanName) {
        if (bean instanceof DruidDataSource) {
            log.debug("添加自定义 DruidDataSource 数据源过滤器 多租户SQL拦截");
            DruidDataSource druidDataSource = (DruidDataSource) bean;
            druidDataSource.setProxyFilters(Collections.singletonList(new RewriteSqlDruidFilter(this.tenantService, this.tenantProperties)));
        }
        return null;
    }
}
