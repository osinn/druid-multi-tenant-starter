package com.github.osinn.druid.multi.tenant.plugin.support;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.github.osinn.druid.multi.tenant.plugin.enums.DataSourceWrapperEnum;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.starter.TenantProperties;
import com.github.osinn.druid.multi.tenant.plugin.support.druid.RewriteSqlDruidFilter;
import org.springframework.cloud.sleuth.instrument.jdbc.DataSourceWrapper;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * 数据源帮助类
 *
 * @author wency_cai
 */
public class DataSourceHelp {

    public static void checkDataSource(Object bean, DefaultSqlParser defaultSqlParser, TenantProperties tenantProperties) {
        if (DataSourceWrapperEnum.BAOMIDOU_DYNAMIC_DATA_SOURCE.equals(tenantProperties.getDataSourceWrapperType())) {
            if (bean instanceof DynamicRoutingDataSource) {
                addProxyFilter((DynamicRoutingDataSource) bean, defaultSqlParser);
            }
        } else if (DataSourceWrapperEnum.SLEUTH_BAOMIDOU_DYNAMIC_DATA_SOURCE_WRAPPER.equals(tenantProperties.getDataSourceWrapperType())) {
            if (bean instanceof DataSourceWrapper) {
                DataSourceWrapper dataSourceWrapper = (DataSourceWrapper) bean;
                DataSource originalDataSource = dataSourceWrapper.getOriginalDataSource();
                if (originalDataSource instanceof DynamicRoutingDataSource) {
                    DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) originalDataSource;
                    addProxyFilter(dynamicRoutingDataSource, defaultSqlParser);
                }
            }
        }
    }

    private static void addProxyFilter(DynamicRoutingDataSource dynamicRoutingDataSource, DefaultSqlParser defaultSqlParser) {
        for (Map.Entry<String, DataSource> entry : dynamicRoutingDataSource.getDataSources().entrySet()) {
            DataSource dynamicDataSource = entry.getValue();
            if (dynamicDataSource instanceof ItemDataSource) {
                ItemDataSource itemDataSource = (ItemDataSource) dynamicDataSource;
                DataSource realDataSource = itemDataSource.getRealDataSource();
                if (realDataSource instanceof DruidDataSource) {
                    DruidDataSource druidDataSource = (DruidDataSource) realDataSource;
                    boolean isCreateProxyFilter = false;
                    for (Filter proxyFilter : druidDataSource.getProxyFilters()) {
                        if (proxyFilter instanceof RewriteSqlDruidFilter) {
                            isCreateProxyFilter = true;
                            break;
                        }
                    }
                    if (!isCreateProxyFilter) {
                        // 如果代理过滤器不存在，则添加代理过滤器
                        druidDataSource.setProxyFilters(Collections.singletonList(new RewriteSqlDruidFilter(defaultSqlParser)));
                    }
                }
            }
        }
    }
}
