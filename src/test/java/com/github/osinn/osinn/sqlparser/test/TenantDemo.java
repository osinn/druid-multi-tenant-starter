package com.github.osinn.osinn.sqlparser.test;

import com.alibaba.druid.DbType;
import com.alibaba.druid.util.JdbcConstants;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author wency_cai
 */
public class TenantDemo {

    public static void main(String[] args) {
        DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
        TenantServiceImpl tenantService = new TenantServiceImpl();
        defaultSqlParser.setTenantService(tenantService);
        defaultSqlParser.setTenantInfoHandler(new TenantInfoHandler() {
            @Override
            public List getTenantIds() {
                Long tenantId = 1540616714700591104L;
                Long tenantId2 = 1540616731523944448L;
                List<Long> tenantIdList = new ArrayList<>();
                tenantIdList.add(tenantId);
//                tenantIdList.add(tenantId2);
                return tenantIdList;
            }

            @Override
            public DbType getDbType() {
                return JdbcConstants.MYSQL;
            }

            @Override
            public List<String> ignoreTableName() {
                List<String> objects = new ArrayList<>();
                objects.add("role");
                return objects;
            }

            @Override
            public List<String> ignoreMatchTableAlias() {
                List<String> objects = new ArrayList<>();
                objects.add("temp");
                return objects;
            }

            @Override
            public List<String> ignoreTableNamePrefix() {
                List<String> objects = new ArrayList<>();
                objects.add("act_");
                return objects;
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public List<String> ignoreDynamicDatasource() {
                return null;
            }
        });

//        int index = 0;
//        boolean outSQL = false;
//        for (Map.Entry<String, String> item : ExplainResult.getExplainResult().entrySet()) {
//            String explainSQL = item.getKey();
//            String result = item.getValue();
//            index++;
//            String tenantSQL = "";
//            try {
//                tenantSQL = defaultSqlParser.setTenantParameter(explainSQL);
//                System.out.println("执行完：" + index);
//            } catch (Exception e) {
//                System.out.println("SQL解析异常，索引：" + index);
//                e.printStackTrace();
//            }
//            if (outSQL && index >= 46) {
//                System.out.println(tenantSQL);
//                System.out.println("------------------------------------- \n");
//            }
//
//            if (!outSQL) {
//                if (!tenantSQL.equals(result)) {
//                    System.out.println("SQL解析对比异常，索引：" + index);
//                    System.out.println("原SQL   -------------------------------------------------------------------------- \n" + explainSQL);
//                    System.out.println("预期SQL -------------------------------------------------------------------------- \n" + result);
//                    System.out.println("结果SQL -------------------------------------------------------------------------- \n" + tenantSQL);
//                    throw new RuntimeException("SQL错误");
//                }
//            }
//        }


        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql1));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql2));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql3));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql4));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql5));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql6));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql7));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql8));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql9));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql10));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql11));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql12));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql13));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql14));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql15));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql16));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql17));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql18));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql19));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql20));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql21));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql22));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql23));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql24));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql25));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql26));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql27));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql28));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql29));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql30));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql31));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql32));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql33));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql34));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql35));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql36));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql37));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql38));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql39));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql40));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql41));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql42));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql43));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql44));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql45));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql46));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql47));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql48));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql49));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql50));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql51));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql52));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(ExplainResult.sql53));

    }
}
