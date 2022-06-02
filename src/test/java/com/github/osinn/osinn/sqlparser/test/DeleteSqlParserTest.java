package com.github.osinn.osinn.sqlparser.test;

import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

/**
 * 描述
 *
 * @author wency_cai
 */
public class DeleteSqlParserTest {

    private static final String sql1 = "delete from user where id = 1";
    private static final String sql2 = "delete from user where id in ( select id from user s )";
    private static final String sql3 = "DELETE FROM system_permission_data sp LEFT JOIN system_role_data_relation re ON sp.id=re.data_id LEFT JOIN system_role sr ON sr.id=re.role_id WHERE sr.id=1";


    public static void main(String[] args) {
        DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
        System.out.println(defaultSqlParser.setTenantParameter(sql1));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql2));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql3));

    }
}
