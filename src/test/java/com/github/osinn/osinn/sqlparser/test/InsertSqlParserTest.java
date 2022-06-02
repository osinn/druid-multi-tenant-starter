package com.github.osinn.osinn.sqlparser.test;

import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;

/**
 * 描述
 *
 * @author wency_cai
 */
public class InsertSqlParserTest {

    private static final String sql1 = "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?)";
    private static final String sql2 = "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?) , (?,?,?) , (?,?,?)";

    public static void main(String[] args) {
        DefaultSqlParser defaultSqlParser = new DefaultSqlParser();
//        System.out.println(defaultSqlParser.setTenantParameter(sql1));
        System.out.println(defaultSqlParser.setTenantParameter(sql2));
    }
}
