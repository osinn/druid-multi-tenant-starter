package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.alibaba.druid.sql.parser.ParserException;
import com.github.osinn.druid.multi.tenant.plugin.SqlParserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultSqlParser} 扩展点测试：customizeParser / before / after / skipParser / Mapper 传参。
 *
 * @author wency_cai
 */
class DefaultSqlParserExtensionTest {

    private static void assertSql(String expected, DefaultSqlParser parser, String sql) {
        assertEquals(SqlParserFixture.normalize(expected), SqlParserFixture.normalize(parser.setTenantParameter(sql)),
                "SQL 解析结果不符合预期，原始 SQL: " + sql);
    }

    @Test
    @DisplayName("customizeParser 有返回值时直接返回，不走默认解析")
    void customizeParserTakesOver() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).customizeParser("CUSTOM").build();
        assertSql("CUSTOM", parser, "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("customizeParser 优先于 after")
    void customizeParserWinsOverAfter() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11)
                .customizeParser("CUSTOM").after("AFTER").build();
        assertSql("CUSTOM", parser, "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("before 有返回值时替换原始 SQL 后再做默认解析")
    void beforeReplacesOriginalSql() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11)
                .before("select * from role where id = 1").build();
        assertSql("SELECT * FROM role WHERE id = 1 AND tenant_id = 11", parser, "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("before 返回空时不替换原始 SQL")
    void beforeReturnsNullKeepsOriginalSql() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).build();
        assertSql("SELECT * FROM user WHERE name = 'a' AND tenant_id = 11", parser, "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("after 有返回值时覆盖默认解析结果")
    void afterOverridesResult() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).after("AFTER").build();
        assertSql("AFTER", parser, "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("Mapper 传入租户 ID：单个值优先于 handler 提供的租户集合")
    void mapperParamTenantIdOverridesHandler() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).build();
        assertEquals("select * from user where name = 'a' and tenant_id = 77",
                SqlParserFixture.normalize(parser.setTenantParameter(null, "select * from user where name = 'a'", 77L)));
    }

    @Test
    @DisplayName("Mapper 传入租户 ID：支持集合，生成 IN 条件")
    void mapperParamTenantIdCollection() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds().build();
        assertEquals("select * from user where name = 'a' and tenant_id in (77, 88)",
                SqlParserFixture.normalize(parser.setTenantParameter(null, "select * from user where name = 'a'",
                        Arrays.asList(77L, 88L))));
    }

    @Test
    @DisplayName("Mapper 未传租户 ID 时回退到 handler 提供的租户集合")
    void mapperParamTenantIdFallsBackToHandler() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).build();
        assertEquals("select * from user where name = 'a' and tenant_id = 11",
                SqlParserFixture.normalize(parser.setTenantParameter(null, "select * from user where name = 'a'", null)));
    }

    @Test
    @DisplayName("skipParser 标记本身可被读取（实际跳过判断由 mybatis/jpa/druid 拦截层负责）")
    void skipParserFlagIsReadable() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).skipParser(true).build();
        assertTrue(parser.isSkipParser());
        // setTenantParameter 自身不做跳过判断，调用方需先调用 isSkipParser()
        assertSql("SELECT * FROM user WHERE name = 'a' AND tenant_id = 11", parser, "select * from user where name = 'a'");
    }

    @Test
    @DisplayName("空 SQL 返回空字符串")
    void emptySqlReturnsEmpty() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).build();
        assertEquals("", parser.setTenantParameter(""));
    }

    @Test
    @DisplayName("非法 SQL 抛出 druid ParserException")
    void invalidSqlThrowsParserException() {
        DefaultSqlParser parser = SqlParserFixture.parser().tenantIds(11).build();
        assertThrows(ParserException.class, () -> parser.setTenantParameter("this is not sql"));
    }
}
