package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.github.osinn.druid.multi.tenant.plugin.SqlParserFixture;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 租户隔离边界场景测试。
 *
 * @author wency_cai
 */
class DefaultSqlParserEdgeCaseTest {

    private static String parse(String sql) {
        return SqlParserFixture.normalize(SqlParserFixture.parser().tenantIds(11).build().setTenantParameter(sql));
    }

    // -------------------------------------------------- where 顶层为 OR 的优先级

    @Test
    @DisplayName("where 顶层为 OR 时，租户条件必须与整体做 AND（否则租户隔离失效）")
    void orConditionMustBeParenthesized() {
        assertEquals("select * from user where (a = 1 or b = 2) and tenant_id = 11",
                parse("select * from user where a = 1 or b = 2"));
        assertEquals("select * from user where (name = 'a' or age = 1) and tenant_id = 11",
                parse("select * from user where name = 'a' or age = 1"));
        assertEquals("select * from user where (a = 1 or b = 2 or c = 3) and tenant_id = 11",
                parse("select * from user where (a = 1 or b = 2) or c = 3"));
        // 原本就带括号的场景不应产生多余括号
        assertEquals("select * from user where (a = 1 or b = 2) and tenant_id = 11",
                parse("select * from user where (a = 1 or b = 2)"));
    }

    @Test
    @DisplayName("update / delete 的 where 顶层为 OR 时同样要加括号")
    void updateAndDeleteOrConditionMustBeParenthesized() {
        assertEquals("update user set name = 'a' where (a = 1 or b = 2) and tenant_id = 11",
                parse("update user set name = 'a' where a = 1 or b = 2"));
        assertEquals("delete from user where (a = 1 or b = 2) and tenant_id = 11",
                parse("delete from user where a = 1 or b = 2"));
    }

    @Test
    @DisplayName("where 顶层为 AND 时保持原有行为，不多加括号")
    void andConditionKeepsBehavior() {
        assertEquals("select * from user where a = 1 and b = 2 and tenant_id = 11",
                parse("select * from user where a = 1 and b = 2"));
        assertEquals("select * from user where (a = 1 or b = 2) and c = 3 and tenant_id = 11",
                parse("select * from user where (a = 1 or b = 2) and c = 3"));
    }

    // ---------------------------------------------------- where 中的各种子查询

    @Test
    @DisplayName("where 中的 exists / not exists 子查询必须被解析并追加租户条件")
    void existsSubqueryInWhereMustBeProcessed() {
        assertEquals("select * from user u where exists ( select 1 from role r where r.id = u.role_id "
                        + "and r.tenant_id = 11 ) and u.tenant_id = 11",
                parse("select * from user u where exists (select 1 from role r where r.id = u.role_id)"));
        assertEquals("select * from user u where not exists ( select 1 from role r where r.id = u.role_id "
                        + "and r.tenant_id = 11 ) and u.tenant_id = 11",
                parse("select * from user u where not exists (select 1 from role r where r.id = u.role_id)"));
    }

    @Test
    @DisplayName("where 中的 exists 子查询带自己的 where 条件时也要追加租户条件")
    void existsSubqueryWithOwnWhere() {
        assertEquals("select * from user u where exists ( select 1 from role r where r.status = 1 "
                        + "and r.tenant_id = 11 ) and u.tenant_id = 11",
                parse("select * from user u where exists (select 1 from role r where r.status = 1)"));
    }

    @Test
    @DisplayName("where 中的标量子查询必须被解析")
    void scalarSubqueryInWhereMustBeProcessed() {
        assertEquals("select * from user where ( select count(*) from role r where r.tenant_id = 11 ) > 1 "
                        + "and tenant_id = 11",
                parse("select * from user where (select count(*) from role r) > 1"));
    }

    // ------------------------------------------------------------------ CTE

    @Test
    @DisplayName("with(CTE) 应给内部真实表加租户条件，且不给 CTE 名加条件")
    void cteMustProcessInnerTables() {
        assertEquals("with t as ( select * from user where id = 1 and tenant_id = 11 ) select * from t",
                parse("with t as (select * from user where id = 1) select * from t"));
        assertEquals("with t as ( select * from user where tenant_id = 11 ) select * from t where t.id > 0",
                parse("with t as (select * from user) select * from t where t.id > 0"));
    }

    @Test
    @DisplayName("with(CTE) 内部查询自带租户条件时不重复拼接")
    void cteInnerAlreadyHasTenantCondition() {
        assertEquals("with t as ( select * from user where tenant_id = 5 ) select * from t",
                parse("with t as (select * from user where tenant_id = 5) select * from t"));
    }

    @Test
    @DisplayName("CTE 体内的同名真实表仍要加租户条件（名称遮蔽不得造成隔离泄漏）")
    void cteBodyShadowedRealTableMustStillGetTenantCondition() {
        // CTE 体内的 user 指向真实表，必须加条件；外层 user 是 CTE 名，不加条件
        assertEquals("with user as ( select * from user where deleted = 0 and tenant_id = 11 ) select * from user",
                parse("with user as (select * from user where deleted = 0) select * from user"));
        // 第一个 CTE 体内的 user 是真实表，不能受「后面才定义的同名 CTE」影响
        assertEquals("with t as ( select * from user where tenant_id = 11 ), user as ( select * from role"
                        + " where tenant_id = 11 ) select * from t, user",
                parse("with t as (select * from user), user as (select * from role) select * from t, user"));
    }

    @Test
    @DisplayName("非递归 CTE 的别名只对其后的 CTE 和主语句可见")
    void cteAliasVisibleToLaterCtesAndMainQueryOnly() {
        assertEquals("with t1 as ( select * from user where tenant_id = 11 ), t2 as ( select * from t1 )"
                        + " select * from t2",
                parse("with t1 as (select * from user), t2 as (select * from t1) select * from t2"));
        assertEquals("with user as ( select * from role where tenant_id = 11 ) select * from user u"
                        + " join menu m on m.id = u.id and m.tenant_id = 11",
                parse("with user as (select * from role) select * from user u join menu m on m.id = u.id"));
    }

    @Test
    @DisplayName("嵌套 with 的内层别名不能泄漏到内层语句之外")
    void nestedWithAliasMustNotLeakOutOfScope() {
        assertEquals("with a as ( with b as ( select * from user where tenant_id = 11 ) select * from b )"
                        + " select * from a",
                parse("with a as (with b as (select * from user) select * from b) select * from a"));
    }

    @Test
    @DisplayName("递归 CTE 的别名对自身定义体也可见")
    void recursiveCteAliasVisibleInItsOwnBody() {
        assertEquals("with recursive t as ( select id from user where tenant_id = 11"
                        + " union all select id from t ) select * from t",
                parse("with recursive t as (select id from user union all select id from t) select * from t"));
    }

    @Test
    @DisplayName("带列别名定义的 CTE 不影响内部真实表处理")
    void cteWithColumnAlias() {
        assertEquals("with t (a, b) as ( select id, name from user where tenant_id = 11 ) select * from t",
                parse("with t (a, b) as (select id, name from user) select * from t"));
    }

    @Test
    @DisplayName("from 子查询里内联的 with 也要正确处理")
    void inlineWithInsideFromSubquery() {
        assertEquals("select * from ( with t as ( select * from user where tenant_id = 11 ) select * from t ) x",
                parse("select * from (with t as (select * from user) select * from t) x"));
    }

    @Test
    @DisplayName("with + update：CTE 体内加条件，主语句引用 CTE 名不加条件")
    void withUpdate() {
        assertEquals("with t as ( select * from user where tenant_id = 11 ) update role set name = 'a'"
                        + " where id in ( select id from t ) and tenant_id = 11",
                parse("with t as (select * from user) update role set name = 'a' where id in (select id from t)"));
    }

    // ------------------------------------------------- 列名以 tenant_id 开头

    @Test
    @DisplayName("列名只是以 tenant_id 开头时不应被当作已有租户条件")
    void tenantIdPrefixedPropertyMustNotBeTreatedAsTenantCondition() {
        assertEquals("select * from user u where u.tenant_id_flag = 1 and u.tenant_id = 11",
                parse("select * from user u where u.tenant_id_flag = 1"));
        assertEquals("select * from user where tenant_id_log = 1 and tenant_id = 11",
                parse("select * from user where tenant_id_log = 1"));
    }

    @Test
    @DisplayName("insert 列表中有 tenant_id 前缀列时仍应补租户列")
    void insertWithTenantIdPrefixedColumnMustStillAddTenant() {
        assertEquals("insert into user (id, tenant_id_flag, tenant_id) values (1, 2, 11)",
                SqlParserFixture.normalize(SqlParserFixture.parser().tenantIds(11).build()
                        .setTenantParameter("INSERT INTO user (id, tenant_id_flag) VALUES (1, 2)")));
    }

    @Test
    @DisplayName("列名大小写不同时仍能识别为已有租户条件")
    void tenantIdColumnMatchIsCaseInsensitive() {
        assertEquals("select * from user where tenant_id = 99", parse("select * from user where TENANT_ID = 99"));
        assertEquals("select * from user u where u.tenant_id in (1, 2)", parse("select * from user u where u.Tenant_Id in (1,2)"));
    }

    @Test
    @DisplayName("带反引号的租户列仍能被识别（insert 不重复加列、where 不重复拼条件）")
    void tenantIdColumnWithBacktick() {
        assertEquals("insert into `user` (`id`, `username`, `tenant_id`) values (?, ?, ?)",
                SqlParserFixture.normalize(SqlParserFixture.parser().tenantIds(11).build()
                        .setTenantParameter("INSERT INTO `user` (`id`,`username`,`tenant_id`) VALUES (?,?,?)")));
        assertEquals("select * from user where `tenant_id` = 99", parse("select * from user where `tenant_id` = 99"));
        assertEquals("select * from `user` where `tenant_id` = 99", parse("select * from `user` where `tenant_id` = 99"));
    }

    @Test
    @DisplayName("CTE 别名作用域必须随语句结束释放，不能串到下一次解析")
    void cteAliasScopeMustBeReleasedAfterStatement() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).build();
        assertEquals("with t as ( select * from user where tenant_id = 11 ) select * from t",
                SqlParserFixture.normalize(parser.setTenantParameter("with t as (select * from user) select * from t")));
        // 同一 parser 连续解析：t 已不再是 CTE，必须重新拼接租户条件
        assertEquals("select * from t where id = 1 and tenant_id = 11",
                SqlParserFixture.normalize(parser.setTenantParameter("select * from t where id = 1")));
    }

    @Test
    @DisplayName("非法 SQL 解析失败后 CTE 上下文不应残留")
    void cteAliasScopeMustNotLeakAfterParseFailure() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).build();
        assertThrows(RuntimeException.class, () -> parser.setTenantParameter("select * from where"));
        assertEquals("select * from t where id = 1 and tenant_id = 11",
                SqlParserFixture.normalize(parser.setTenantParameter("select * from t where id = 1")));
    }

    // ------------------------------------------------------- 其它健壮性问题

    @Test
    @DisplayName("配置多个忽略数据源时每一个都应参与比较")
    void ignoreDynamicDatasourceMustCompareAllConfiguredNames() {
        assertTrue(SqlParserFixture.parser().ignoreDynamicDatasource("a", "b").currentDatasource("b")
                .build().isIgnoreDynamicDatasource(), "当前数据源命中配置列表第 2 个元素时也应被忽略");
        assertTrue(SqlParserFixture.parser().ignoreDynamicDatasource("a", "b", "c").currentDatasource("c")
                .build().isIgnoreDynamicDatasource(), "当前数据源命中配置列表第 3 个元素时也应被忽略");
    }

    @Test
    @DisplayName("null SQL 不应抛 NPE")
    void nullSqlMustNotThrowNpe() {
        assertDoesNotThrow(() -> SqlParserFixture.parser().tenantIds(11).build().setTenantParameter(null));
    }
}
