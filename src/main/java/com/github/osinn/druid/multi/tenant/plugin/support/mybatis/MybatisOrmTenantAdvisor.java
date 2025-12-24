package com.github.osinn.druid.multi.tenant.plugin.support.mybatis;

import com.alibaba.druid.DbType;
import com.github.osinn.druid.multi.tenant.plugin.annotation.IgnoreTenantIdField;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import com.github.osinn.druid.multi.tenant.plugin.support.BaseOrmTenantAdvisor;
import com.github.osinn.druid.multi.tenant.plugin.starter.TenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;

import java.lang.reflect.Method;
import java.util.List;

/**
 * MyBatis 拦截器实现方式
 *
 * @author wency_cai
 */
@Slf4j
public class MybatisOrmTenantAdvisor implements BaseOrmTenantAdvisor {

    private final ITenantService tenantService;

    private final TenantProperties tenantProperties;

    public MybatisOrmTenantAdvisor(ITenantService tenantService, TenantProperties tenantProperties) {
        this.tenantService = tenantService;
        this.tenantProperties = tenantProperties;
    }


    private MybatisMultiTenantPluginInterceptor createMybatisMultiTenantPluginInterceptor() {
        return new MybatisMultiTenantPluginInterceptor(tenantService, new TenantInfoHandler() {

            @Override
            public List<Object> getTenantIds() {
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
            public List<String> ignoreTableNamePrefix() {
                return tenantProperties.getIgnoreTableNamePrefix();
            }

            @Override
            public String getTenantIdColumn() {
                return tenantProperties.getTenantIdColumn();
            }

            @Override
            public List<String> ignoreDynamicDatasource() {
                return tenantProperties.getIgnoreDynamicDatasource();
            }

            @Override
            public DbType getDbType() {
                return tenantProperties.getDbType();
            }

        });
    }

    @Override
    public Object doHandle(Object bean, String beanName) {
        if (bean instanceof SqlSessionFactory) {
            log.debug("添加自定义Mybatis多租户SQL拦截器");
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
            // 添加拦截器
            sqlSessionFactory.getConfiguration().addInterceptor(createMybatisMultiTenantPluginInterceptor());
        }
        if (bean instanceof MapperFactoryBean) {
            MapperFactoryBean mapperFactoryBean = (MapperFactoryBean) bean;
            final String mapperName = mapperFactoryBean.getObjectType().getName();
            Method[] methods = mapperFactoryBean.getObjectType().getMethods();
            for (Method item : methods) {
                IgnoreTenantIdField annotation = item.getAnnotation(IgnoreTenantIdField.class);
                if (annotation != null) {
                    TenantInfoHandler.IGNORE_TENANT_ID_METHODS.add(String.format("%s.%s", mapperName, item.getName()));
                }
            }
        }
        return bean;
    }
}
