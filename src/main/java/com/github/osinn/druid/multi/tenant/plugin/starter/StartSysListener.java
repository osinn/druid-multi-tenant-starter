package com.github.osinn.druid.multi.tenant.plugin.starter;

import com.alibaba.druid.DbType;
import com.github.osinn.druid.multi.tenant.plugin.MybatisMultiTenantPluginInterceptor;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

/**
 * @author wency_cai
 */
public class StartSysListener implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LoggerFactory.getLogger(StartSysListener.class);

    private final List<SqlSessionFactory> sqlSessionFactoryList;

    private final ITenantService tenantService;

    private final TenantProperties tenantProperties;

    public StartSysListener(ITenantService tenantService, TenantProperties tenantProperties, List<SqlSessionFactory> sqlSessionFactoryList) {
        this.tenantService = tenantService;
        this.tenantProperties = tenantProperties;
        this.sqlSessionFactoryList = sqlSessionFactoryList;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("添加自定义Mybatis多租户SQL拦截器");
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            // 添加拦截器
            sqlSessionFactory.getConfiguration().addInterceptor(new MybatisMultiTenantPluginInterceptor(new TenantInfoHandler() {

                @Override
                public List getTenantIds() {
                    return tenantService.getTenantIds();
                }

                @Override
                public List<String> ignoreTableName() {
                    return tenantProperties.getIgnoreTableName();
                }

                @Override
                public List<String> ignoreMatchTableAlias() {
                    return tenantProperties.getIgnoreMatchTableAlias();
                }

                @Override
                public String getTenantIdColumn() {
                    return tenantProperties.getTenantIdColumn();
                }

                @Override
                public DbType getDbType() {
                    return tenantProperties.getDbType();
                }

            }));
        }
    }
}