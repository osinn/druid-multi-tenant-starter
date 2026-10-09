package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.github.osinn.druid.multi.tenant.plugin.SqlParserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 忽略规则（忽略表名 / 表名前缀 / 表别名 / 动态数据源）测试。
 *
 * @author wency_cai
 */
class DefaultSqlParserIgnoreRuleTest {

    private static void assertSql(String expected, DefaultSqlParser parser, String sql) {
        assertEquals(SqlParserFixture.normalize(expected), SqlParserFixture.normalize(parser.setTenantParameter(sql)),
                "SQL 解析结果不符合预期，原始 SQL: " + sql);
    }

    @Test
    @DisplayName("忽略表名：全等匹配，带反引号也能识别")
    void ignoreTableName() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).ignoreTableName("user_role").build();
        assertSql("SELECT * FROM user_role WHERE id = 1", parser, "select * from user_role where id = 1");
        assertSql("SELECT * FROM `user_role` WHERE id = 1", parser, "select * from `user_role` where id = 1");
        assertSql("DELETE FROM user_role WHERE id = 1", parser, "delete from user_role where id = 1");
        assertSql("UPDATE user_role SET name = 'a' WHERE id = 1", parser, "update user_role set name = 'a' where id = 1");
        assertSql("INSERT INTO user_role (id, name) VALUES (1, 'a')", parser,
                "INSERT INTO user_role (id, name) VALUES (1,'a')");
        // 未配置忽略的表仍然要加租户条件
        assertSql("SELECT * FROM user WHERE id = 1 AND tenant_id = 11", parser, "select * from user where id = 1");
    }

    @Test
    @DisplayName("忽略表名前缀：前缀匹配")
    void ignoreTableNamePrefix() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).ignoreTableNamePrefix("act_").build();
        assertSql("SELECT * FROM act_log WHERE id = 1", parser, "select * from act_log where id = 1");
        assertSql("SELECT * FROM action_log WHERE id = 1 AND tenant_id = 11", parser,
                "select * from action_log where id = 1");
    }

    @Test
    @DisplayName("忽略表别名：只忽略该别名，子查询内部真实表仍会加条件")
    void ignoreTableAlias() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).ignoreTableAlias("temp").build();
        assertSql("SELECT a, b FROM ( SELECT * FROM table_a WHERE tenant_id = 11 ) temp WHERE temp.a = 'a'",
                parser, "select a, b from (select * from table_a) temp where temp.a = 'a'");
    }

    @Test
    @DisplayName("忽略规则不影响其它表：join 中仍会给未忽略的表加条件")
    void ignoreRuleAppliesPerTable() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).ignoreTableName("user_role").build();
        assertSql("SELECT u.* FROM user u JOIN user_role ur ON ur.user_id = u.id WHERE u.tenant_id = 11",
                parser, "SELECT u.* FROM user u JOIN user_role ur ON ur.user_id = u.id");
    }

    @Test
    @DisplayName("动态数据源忽略：命中配置的数据源时 return true")
    void ignoreDynamicDatasourceHit() {
        assertTrue(SqlParserFixture.parser().ignoreDynamicDatasource("a").currentDatasource("a")
                .build().isIgnoreDynamicDatasource());
        assertTrue(SqlParserFixture.parser().ignoreDynamicDatasource("a", "b").currentDatasource("a")
                .build().isIgnoreDynamicDatasource());
    }

    @Test
    @DisplayName("动态数据源忽略：未配置或未命中时 return false")
    void ignoreDynamicDatasourceMiss() {
        assertFalse(SqlParserFixture.parser().tenantIds(11).build().isIgnoreDynamicDatasource());
        assertFalse(SqlParserFixture.parser().ignoreDynamicDatasource("a", "b").currentDatasource("c")
                .build().isIgnoreDynamicDatasource());
        // 配置了忽略列表但当前数据源为空
        assertFalse(SqlParserFixture.parser().ignoreDynamicDatasource("a", "b")
                .build().isIgnoreDynamicDatasource());
        // 当前数据源非空但未配置忽略列表
        assertFalse(SqlParserFixture.parser().currentDatasource("a").build().isIgnoreDynamicDatasource());
    }
}
