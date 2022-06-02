package com.github.osinn.osinn.sqlparser.test;

import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

/**
 * 描述
 *
 * @author wency_cai
 */
public class SelectSqlParserTest {

    private static final String sql1 = "SELECT id, name,tenant_id FROM role";
    private static final String sql2 = "select * from user s where s.name='333'";
    private static final String sql3 = "select tenant_id from people where id in (select id from user s)";
    private static final String sql4 = "select tenant_id from people where id in (select id from user s) and id in (select u.id from user u)";
    private static final String sql5 = "select tenant_id from people where id " +
            "in (select id from user s) " +
            "and id in (select u.id from user u) " +
            "and tenant_id in (select u.tenant_id from user u)";
    private static final String sql6 = "select p.tenant_id, (select u.name from user u where u.id=p.user_id) from people p";
    private static final String sql7 = "select p.tenant_id, (select u.name from user u where u.id=p.user_id), (select s.name from user s where s.id=p.user_id) from people p";
    private static final String sql8 = "select p.tenant_id, (select u.name, (select s.name from user s where s.id=p.user_id) from user u where u.id=p.user_id) from people p";
    private static final String sql9 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id";
    private static final String sql10 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 22";
    private static final String sql11 = "SELECT u.*, (select ur.name from role r where r.id=ur.role_id) as r_name FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 22";
    private static final String sql12 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND ur.role_id in (select r.id from role r)";
    private static final String sql13 = "SELECT u.* FROM `user` u JOIN user_role ur ON ur.user_id = u.id AND ur.role_id in (select r.id from role r) AND ur.test_id in (select r.test_id from role r)";
    private static final String sql14 = "SELECT u.* FROM `user` u LEFT JOIN user_role ur ON ur.user_id = u.id";
    private static final String sql15 = "SELECT u.* FROM `user` u RIGHT JOIN user_role ur ON ur.user_id = u.id";
    private static final String sql16 = "SELECT COUNT( * ), id FROM `user` GROUP BY id HAVING COUNT( * ) >= 1";
    private static final String sql17 = "SELECT EXISTS ( SELECT * FROM `user` WHERE username = ?) d";
    private static final String sql18 = "SELECT EXISTS ( select tenant_id from people where id in (select id from user s)) d";
    private static final String sql19 = "SELECT username FROM `user`  union SELECT username FROM account";
    private static final String sql20 = "SELECT u.*,r.id AS r_id,r.NAME AS r_name FROM `user` u LEFT JOIN user_role ur ON ur.user_id=u.id LEFT JOIN role r ON r.id=ur.role_id AND u.id=22";
    private static final String sql21 = "SELECT u.*,r.id AS r_id,r.NAME AS r_name FROM `user` u LEFT JOIN user_role ur ON ur.user_id=u.id LEFT JOIN role r ON r.id=ur.role_id AND u.id=22 LEFT JOIN menu m on m.role_id = r.id";
    private static final String sql22 = "select a,b from (select * from table_a) temp where temp.a = 'a';";
    private static final String sql23 = "SELECT temp.* FROM (SELECT * FROM `user`) temp JOIN user_role ur ON ur.user_id=temp.id WHERE temp.a='a';";

    private static final String sql24 = "SELECT id, name,tenant_id FROM role where tenant_id=1";

    public static void main(String[] args) {
        DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
        System.out.println(defaultSqlParser.setTenantParameter(sql1));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql2));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql3));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql4));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql5));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql6));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql7));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql8));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql9));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql10));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql11));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql12));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql13));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql14));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql15));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql16));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql17));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql18));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql19));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql20));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql21));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql22));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql23));
        System.out.println("------------------------------------- \n");
        System.out.println(defaultSqlParser.setTenantParameter(sql24));
    }
}
