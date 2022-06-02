package com.github.osinn.osinn.sqlparser.test;

import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

/**
 * 描述
 *
 * @author wency_cai
 */
public class UpdateSqlParserTest {

    private static final String sql1 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id =?";
    private static final String sql2 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(1,2,3)";
    private static final String sql3 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(SELECT ur.user_id from user_role ur)";
    private static final String sql4 = "update user u set ds=?, u.name=?,id='fdf' ,ddd=? where id in(SELECT ur.user_id from user_role ur where ur.id=1)";
    private static final String sql5 = "UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 111 SET u.qr_code='1212'";
    private static final String sql6 = "UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id in(SELECT urr.user_id from user_role urr) SET u.qr_code='1212'";
    private static final String sql7 = "update user" +
                                        "    set name =" +
                                        "    case " +
                                        "        when id = 1 then 'name1' " +
                                        "        when id = 2 then 'name2' " +
                                        "        when id = 3 then 'name3' " +
                                        "    end " +
                                        "    where id in (11,22,33,3);";

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
    }
}
