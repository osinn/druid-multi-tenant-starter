package com.github.osinn.osinn.sqlparser.test;

import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author wency_cai
 */
public class TenantDemo {

    private static final String sql1 = "SELECT id, name,tenant_id FROM role";

    public static void main(String[] args) {
        DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
        defaultSqlParser.setTenantInfoHandler(new TenantInfoHandler() {
            @Override
            public List getTenantIds() {
                Long tenantId = 12121L;
                Long tenantId2 = 111111L;
                List<Long> tenantIdList = new ArrayList<>();
                tenantIdList.add(tenantId);
                tenantIdList.add(tenantId2);
                return tenantIdList;
            }

            @Override
            public List<String> ignoreTablePrefix() {
                return new ArrayList<>();
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }
        });
        // todo 有问题
        String newSQL = defaultSqlParser.setTenantParameter(sql1);
        System.out.println(newSQL);
    }
}
