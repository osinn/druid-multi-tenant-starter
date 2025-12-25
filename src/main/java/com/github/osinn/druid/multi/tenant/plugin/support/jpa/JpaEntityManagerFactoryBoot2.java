package com.github.osinn.druid.multi.tenant.plugin.support.jpa;

import com.github.osinn.druid.multi.tenant.plugin.context.TenantApplicationContext;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 提供 Spring Boot2 获取 JPA EntityManagerFactory 支持
 *
 * @author wency_cai
 */
public class JpaEntityManagerFactoryBoot2 implements IJpaEntityManagerFactory {

    @Override
    public String getDataSourceUrl() {
        EntityManagerFactory emf = TenantApplicationContext.getBean(EntityManagerFactory.class);
        DataSource dataSource = (DataSource) emf.getProperties().get("hibernate.connection.datasource");
        String url;
        try (Connection connection = dataSource.getConnection()) {
            url = connection.getMetaData().getURL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }
}
