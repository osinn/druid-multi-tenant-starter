package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.github.osinn.druid.multi.tenant.plugin.SqlParserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DefaultSqlParser} 对 select / insert / update / delete 的解析结果测试。
 *
 * @author wency_cai
 */
class DefaultSqlParserSqlTest {

    private static DefaultSqlParser parser11() {
        return SqlParserFixture.parser().tenantIds(11).build();
    }

    private static void assertSql(String expected, DefaultSqlParser parser, String sql) {
        assertEquals(SqlParserFixture.normalize(expected), SqlParserFixture.normalize(parser.setTenantParameter(sql)),
                "SQL 解析结果不符合预期，原始 SQL: " + sql);
    }

    private static void assertSql(String expected, String sql) {
        assertSql(expected, parser11(), sql);
    }

    // ------------------------------------------------------------------ select

    @Test
    @DisplayName("select：带 where 时在原有条件后追加租户条件")
    void selectWithWhere() {
        assertSql("SELECT * FROM user WHERE name = 'a' AND tenant_id = 11",
                "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("select：无 where 时直接补上 where 租户条件")
    void selectWithoutWhere() {
        assertSql("SELECT * FROM user WHERE tenant_id = 11", "select * from user");
    }

    @Test
    @DisplayName("select：表别名会被带到租户条件上")
    void selectWithTableAlias() {
        assertSql("SELECT * FROM user s WHERE s.name = '333' AND s.tenant_id = 11",
                "select * from user s where s.name='333'");
    }

    @Test
    @DisplayName("select：反引号表名与 select 列表中的 tenant_id 不影响条件追加")
    void selectWithBacktickTable() {
        assertSql("SELECT id, name, tenant_id FROM `role` WHERE tenant_id = 11",
                "SELECT id, name, tenant_id FROM `role`");
    }

    @Test
    @DisplayName("select：count 聚合、order by / limit")
    void selectWithAggregateAndLimit() {
        assertSql("SELECT count(*) FROM user WHERE tenant_id = 11", "select count(*) from user");
        assertSql("SELECT * FROM user WHERE id > 1 AND tenant_id = 11 ORDER BY id DESC LIMIT 10",
                "select * from user where id > 1 order by id desc limit 10");
    }

    @Test
    @DisplayName("select：group by / having")
    void selectWithGroupBy() {
        assertSql("SELECT COUNT(*), id FROM `user` WHERE tenant_id = 11 GROUP BY id HAVING COUNT(*) >= 1",
                "SELECT COUNT(*), id FROM `user` GROUP BY id HAVING COUNT(*) >= 1");
    }

    @Test
    @DisplayName("select：占位符与 for update")
    void selectWithPlaceholderAndForUpdate() {
        assertSql("SELECT * FROM user WHERE id = ? AND name = ? AND tenant_id = 11",
                "select * from user where id = ? and name = ?");
        assertSql("SELECT * FROM user WHERE id = 1 AND tenant_id = 11 FOR UPDATE",
                "select * from user where id = 1 for update");
    }

    @Test
    @DisplayName("select：schema 前缀表名")
    void selectWithSchemaPrefix() {
        assertSql("SELECT * FROM mydb.user WHERE id = 1 AND tenant_id = 11",
                "select * from mydb.user where id = 1");
        assertSql("SELECT * FROM mydb.user u WHERE u.id = 1 AND u.tenant_id = 11",
                "select * from mydb.user u where u.id = 1");
    }

    @Test
    @DisplayName("select：in 列表条件")
    void selectWithInList() {
        assertSql("SELECT * FROM user WHERE id IN (1, 2, 3) AND tenant_id = 11",
                "select * from user where id in (1,2,3)");
    }

    @Test
    @DisplayName("select：没有 from 的语句不做处理")
    void selectWithoutFrom() {
        assertSql("SELECT 1", "select 1");
    }

    // ------------------------------------------------------- 已存在租户条件

    @Test
    @DisplayName("select：原 SQL 已有租户条件时不再重复拼接")
    void selectAlreadyHasTenantCondition() {
        assertSql("SELECT * FROM user WHERE tenant_id = 99", "select * from user where tenant_id = 99");
        assertSql("SELECT * FROM user u WHERE u.tenant_id = 99", "select * from user u where u.tenant_id = 99");
        assertSql("SELECT * FROM user WHERE tenant_id IN (1, 2)", "select * from user where tenant_id in (1,2)");
        assertSql("SELECT * FROM user u WHERE u.tenant_id IN (1, 2)", "select * from user u where u.tenant_id in (1,2)");
    }

    // --------------------------------------------------------------- 多租户

    @Test
    @DisplayName("select：多个租户 ID 生成 IN 条件")
    void selectWithMultipleTenantIds() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11, 22).build();
        assertSql("SELECT * FROM user WHERE name = 'a' AND tenant_id IN (11, 22)",
                parser, "select * from user where name = 'a'");
        assertSql("SELECT * FROM user u WHERE u.name = 'a' AND u.tenant_id IN (11, 22)",
                parser, "select * from user u where u.name = 'a'");
        assertSql("SELECT * FROM user WHERE id = 1 AND tenant_id IN (11, 22, 33)",
                SqlParserFixture.parser().tenantIds(11, 22, 33).build(), "select * from user where id = 1");
    }

    @Test
    @DisplayName("select：租户 ID 为空集合时不追加任何条件")
    void selectWithEmptyTenantIds() {
        assertSql("SELECT * FROM user WHERE name = 'a'",
                SqlParserFixture.parser().tenantIds().build(), "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("select：字符串类型租户 ID")
    void selectWithStringTenantId() {
        assertSql("SELECT * FROM user WHERE name = 'a' AND tenant_id = 'T-001'",
                SqlParserFixture.parser().tenantIds("T-001").build(), "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("select：自定义租户列名")
    void selectWithCustomTenantColumn() {
        assertSql("SELECT * FROM user WHERE name = 'a' AND org_id = 11",
                SqlParserFixture.parser().tenantIds(11).tenantIdColumn("org_id").build(),
                "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("租户 ID 中的单引号会被转义，不产生 SQL 注入")
    void tenantIdIsEscaped() {
        assertSql("SELECT * FROM user WHERE id = 1 AND tenant_id = 'a''b'",
                SqlParserFixture.parser().tenantIds("a'b").build(), "select * from user where id = 1");
        assertSql("SELECT * FROM user WHERE id = 1 AND tenant_id IN ('a', 'b''c')",
                SqlParserFixture.parser().tenantIds("a", "b'c").build(), "select * from user where id = 1");
    }

    // ------------------------------------------------------------------ join

    @Test
    @DisplayName("join：两表关联，on 与 where 分别追加租户条件")
    void joinTwoTables() {
        assertSql("SELECT u.* FROM user u JOIN user_role ur ON ur.user_id = u.id AND ur.tenant_id = 11 WHERE u.tenant_id = 11",
                "SELECT u.* FROM user u JOIN user_role ur ON ur.user_id = u.id");
    }

    @Test
    @DisplayName("join：三表关联，每张表都追加租户条件")
    void joinThreeTables() {
        assertSql("SELECT u.*, r.id AS r_id FROM user u LEFT JOIN user_role ur ON ur.user_id = u.id AND ur.tenant_id = 11"
                        + " LEFT JOIN role r ON r.id = ur.role_id AND r.tenant_id = 11 WHERE u.tenant_id = 11",
                "SELECT u.*, r.id AS r_id FROM user u LEFT JOIN user_role ur ON ur.user_id = u.id"
                        + " LEFT JOIN role r ON r.id = ur.role_id");
    }

    @Test
    @DisplayName("join：带 where 时 where 中也追加主表租户条件")
    void joinWithWhere() {
        assertSql("SELECT u.* FROM user u JOIN user_role ur ON ur.user_id = u.id AND ur.tenant_id = 11"
                        + " WHERE u.name = 'a' AND u.tenant_id = 11",
                "SELECT u.* FROM user u JOIN user_role ur ON ur.user_id = u.id WHERE u.name = 'a'");
    }

    @Test
    @DisplayName("join：逗号连接被 druid 渲染为 CROSS JOIN，租户条件仍会追加")
    void joinByComma() {
        assertSql("SELECT * FROM user u CROSS JOIN role r ON r.tenant_id = 11 WHERE u.role_id = r.id AND u.tenant_id = 11",
                "select * from user u, role r where u.role_id = r.id");
    }

    // --------------------------------------------------------------- 子查询

    @Test
    @DisplayName("子查询：from 子句中的子查询")
    void subqueryInFrom() {
        assertSql("SELECT a, b FROM ( SELECT * FROM table_a WHERE tenant_id = 11 ) temp WHERE temp.a = 'a'",
                "select a, b from (select * from table_a) temp where temp.a = 'a'");
        assertSql("SELECT * FROM ( SELECT * FROM user u JOIN role r ON r.id = u.role_id AND r.tenant_id = 11"
                        + " WHERE u.tenant_id = 11 ) t",
                "select * from (select * from user u join role r on r.id = u.role_id) t");
    }

    @Test
    @DisplayName("子查询：where in 中的子查询")
    void subqueryInWhereIn() {
        assertSql("SELECT * FROM user WHERE id IN ( SELECT id FROM t2 WHERE tenant_id = 11 ) AND tenant_id = 11",
                "select * from user where id in (select id from t2)");
        assertSql("SELECT * FROM user WHERE id IN ( SELECT id FROM a WHERE tenant_id = 11"
                        + " UNION SELECT id FROM b WHERE tenant_id = 11 ) AND tenant_id = 11",
                "select * from user where id in (select id from a union select id from b)");
    }

    @Test
    @DisplayName("子查询：多层 where in 嵌套")
    void nestedSubqueryInWhere() {
        assertSql("SELECT tenant_id FROM people WHERE id IN ( SELECT id FROM user s WHERE s.tenant_id = 11 )"
                        + " AND id IN ( SELECT u.id FROM user u WHERE u.tenant_id = 11 ) AND tenant_id = 11",
                "select tenant_id from people where id in (select id from user s) and id in (select u.id from user u)");
    }

    @Test
    @DisplayName("子查询：select 列表中的子查询（含多层嵌套）")
    void subqueryInSelectList() {
        assertSql("SELECT p.tenant_id , ( SELECT u.name FROM user u WHERE u.id = p.user_id AND u.tenant_id = 11 )"
                        + " FROM people p WHERE p.tenant_id = 11",
                "select p.tenant_id, (select u.name from user u where u.id = p.user_id) from people p");
        assertSql("SELECT ( SELECT ( SELECT 1 FROM t3 WHERE tenant_id = 11 ) FROM t2 WHERE tenant_id = 11 )"
                        + " FROM user WHERE tenant_id = 11",
                "select (select (select 1 from t3) from t2) from user");
    }

    @Test
    @DisplayName("子查询：select 列表中的 exists")
    void existsInSelectList() {
        assertSql("SELECT EXISTS ( SELECT * FROM user WHERE username = 'x' AND tenant_id = 11 ) AS d"
                        + " FROM dual WHERE tenant_id = 11",
                "SELECT EXISTS (SELECT * FROM user WHERE username = 'x') AS d FROM dual");
    }

    @Test
    @DisplayName("子查询：union 以及 union 作为子查询表")
    void unionQuery() {
        assertSql("SELECT username FROM user WHERE tenant_id = 11 UNION SELECT username FROM account WHERE tenant_id = 11",
                "select username from user union select username from account");
        assertSql("SELECT * FROM ( SELECT id FROM a WHERE tenant_id = 11 UNION SELECT id FROM b WHERE tenant_id = 11 ) t",
                "select * from (select id from a union select id from b) t");
    }

    // ---------------------------------------------------------------- insert

    @Test
    @DisplayName("insert：指定列且不含租户列时补充租户列与值")
    void insertAddTenantColumn() {
        assertSql("INSERT INTO `user` (`id`, `username`, `password`, tenant_id) VALUES (?, ?, ?, 11)",
                "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?)");
    }

    @Test
    @DisplayName("insert：多行 values 每一行都补充租户值")
    void insertMultiRow() {
        assertSql("INSERT INTO `user` (`id`, `username`, `password`, tenant_id) VALUES (?, ?, ?, 11), (?, ?, ?, 11)",
                "INSERT INTO `user` (`id`,`username`,`password`) VALUES (?,?,?) , (?,?,?)");
    }

    @Test
    @DisplayName("insert：已指定租户列时不重复处理")
    void insertAlreadyHasTenantColumn() {
        assertSql("INSERT INTO `user` (`id`, `username`, `tenant_id`) VALUES (?, ?, ?)",
                "INSERT INTO `user` (`id`,`username`,`tenant_id`) VALUES (?,?,?)");
    }

    @Test
    @DisplayName("insert：未指定列时按约定认为包含了租户列，不做处理")
    void insertWithoutColumnList() {
        assertSql("INSERT INTO user VALUES (1, 'a')", "INSERT INTO user VALUES (1,'a')");
    }

    @Test
    @DisplayName("insert select：目标列补租户列，来源查询补租户条件与租户列")
    void insertSelect() {
        assertSql("INSERT INTO role (id, name, tenant_id) SELECT id, name, tenant_id FROM tmp_role WHERE tenant_id = 11",
                "INSERT INTO role (id, name) SELECT id, name FROM tmp_role");
        assertSql("INSERT INTO role SELECT * FROM tmp_role WHERE tenant_id = 11",
                "INSERT INTO role SELECT * FROM tmp_role");
    }

    @Test
    @DisplayName("insert：on duplicate key update 不影响租户列补充")
    void insertOnDuplicateKey() {
        assertSql("INSERT INTO user (id, name, tenant_id) VALUES (1, 'a', 11) ON DUPLICATE KEY UPDATE name = 'b'",
                "insert into user (id, name) values (1, 'a') on duplicate key update name = 'b'");
    }

    // ---------------------------------------------------------------- update

    @Test
    @DisplayName("update：普通更新")
    void updateSimple() {
        assertSql("UPDATE user SET name = 'a' WHERE id = 1 AND tenant_id = 11",
                "update user set name = 'a' where id = 1");
        assertSql("UPDATE user u SET u.name = 'a' WHERE u.id = 1 AND u.tenant_id = 11",
                "update user u set u.name = 'a' where u.id = 1");
    }

    @Test
    @DisplayName("update：无 where 时补充租户条件")
    void updateWithoutWhere() {
        assertSql("UPDATE user SET name = 'a' WHERE tenant_id = 11", "update user set name = 'a'");
    }

    @Test
    @DisplayName("update：join 更新，on 与 where 分别追加租户条件")
    void updateJoin() {
        assertSql("UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 111 AND ur.tenant_id = 11"
                        + " SET u.qr_code = '1212' WHERE u.tenant_id = 11",
                "UPDATE `user` u JOIN user_role ur ON ur.user_id = u.id AND u.id = 111 SET u.qr_code = '1212'");
        assertSql("UPDATE user u JOIN user_role ur ON ur.user_id = u.id AND ur.tenant_id = 11"
                        + " SET u.name = 'n' WHERE ur.id = 1 AND u.tenant_id = 11",
                "update user u join user_role ur on ur.user_id = u.id set u.name = 'n' where ur.id = 1");
    }

    @Test
    @DisplayName("update：in 子查询与已有租户条件")
    void updateSubqueryAndExistingTenant() {
        assertSql("UPDATE user u SET u.name = 'a' WHERE u.id IN ( SELECT ur.user_id FROM user_role ur"
                        + " WHERE ur.tenant_id = 11 ) AND u.tenant_id = 11",
                "update user u set u.name = 'a' where u.id in (SELECT ur.user_id from user_role ur)");
        assertSql("UPDATE user SET name = 'a' WHERE tenant_id = 99",
                "update user set name = 'a' where tenant_id = 99");
    }

    // ---------------------------------------------------------------- delete

    @Test
    @DisplayName("delete：普通删除")
    void deleteSimple() {
        assertSql("DELETE FROM user WHERE id = 1 AND tenant_id = 11", "delete from user where id = 1");
        assertSql("DELETE FROM user WHERE tenant_id = 11", "delete from user");
    }

    @Test
    @DisplayName("delete：join 删除，每张表都追加租户条件")
    void deleteJoin() {
        assertSql("DELETE FROM system_permission_data sp"
                        + " LEFT JOIN system_role_data_relation re ON sp.id = re.data_id AND re.tenant_id = 11"
                        + " LEFT JOIN system_role sr ON sr.id = re.role_id AND sr.tenant_id = 11"
                        + " WHERE sr.id = 1 AND sp.tenant_id = 11",
                "DELETE FROM system_permission_data sp"
                        + " LEFT JOIN system_role_data_relation re ON sp.id = re.data_id"
                        + " LEFT JOIN system_role sr ON sr.id = re.role_id WHERE sr.id = 1");
    }

    @Test
    @DisplayName("delete：别名语法与 in 子查询")
    void deleteAliasAndSubquery() {
        assertSql("DELETE u FROM user u JOIN user_role ur ON ur.user_id = u.id AND ur.tenant_id = 11"
                        + " WHERE ur.id = 1 AND u.tenant_id = 11",
                "delete u from user u join user_role ur on ur.user_id = u.id where ur.id = 1");
        assertSql("DELETE FROM user WHERE id IN ( SELECT id FROM user s WHERE s.tenant_id = 11 ) AND tenant_id = 11",
                "delete from user where id in (select id from user s)");
    }

    @Test
    @DisplayName("delete：已有租户条件时不重复拼接")
    void deleteExistingTenant() {
        assertSql("DELETE FROM user WHERE tenant_id = 99", "delete from user where tenant_id = 99");
    }

    // -------------------------------------------------------------- 多语句

    @Test
    @DisplayName("多语句：每条语句都会被处理并拼接")
    void multiStatement() {
        assertSql("SELECT * FROM user WHERE id = 1 AND tenant_id = 11;SELECT * FROM role WHERE id = 2 AND tenant_id = 11;",
                "select * from user where id = 1; select * from role where id = 2;");
    }
}
