package com.github.osinn.druid.multi.tenant.plugin.support;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.support.druid.RewriteSqlDruidFilter;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * 多租户数据源代理，用于给 baomidou 动态数据源下的 DruidDataSource 添加 druid 过滤器 SQL 解析器
 *
 * @author wency_cai
 */
public class TenantDataSourceProxy {

    private final DefaultSqlParser defaultSqlParser;

    public TenantDataSourceProxy(DefaultSqlParser defaultSqlParser, DataSource dataSource) {
        this.defaultSqlParser = defaultSqlParser;
        // 这里只是简单的判断一下是否为 DruidDataSource 数据源
        if (dataSource instanceof DynamicRoutingDataSource) {
            DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
            this.addProxyFilter(dynamicRoutingDataSource);
        }
    }


    private void addProxyFilter(DynamicRoutingDataSource dynamicRoutingDataSource) {
        for (Map.Entry<String, DataSource> entry : dynamicRoutingDataSource.getDataSources().entrySet()) {
            DataSource dynamicDataSource = entry.getValue();
            if (dynamicDataSource instanceof ItemDataSource) {
                ItemDataSource itemDataSource = (ItemDataSource) dynamicDataSource;
                DataSource realDataSource = itemDataSource.getRealDataSource();
                if (realDataSource instanceof DruidDataSource) {
                    DruidDataSource druidDataSource = (DruidDataSource) realDataSource;
                    boolean createProxyFilter = false;
                    for (Filter proxyFilter : druidDataSource.getProxyFilters()) {
                        if (proxyFilter instanceof RewriteSqlDruidFilter) {
                            createProxyFilter = true;
                            break;
                        }
                    }
                    if (!createProxyFilter) {
                        // 如果代理过滤器不存在，则添加代理过滤器
                        druidDataSource.setProxyFilters(Collections.singletonList(new RewriteSqlDruidFilter(defaultSqlParser)));
                    }
                }
            }
        }
    }
}
