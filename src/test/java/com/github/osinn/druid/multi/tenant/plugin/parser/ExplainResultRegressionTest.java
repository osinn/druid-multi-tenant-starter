package com.github.osinn.druid.multi.tenant.plugin.parser;

import com.github.osinn.druid.multi.tenant.plugin.SqlParserFixture;
import com.github.osinn.osinn.sqlparser.test.ExplainResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExplainResult} 期望数据纳入 JUnit 回归。
 *
 * @author wency_cai
 */
class ExplainResultRegressionTest {

    @Test
    @DisplayName("ExplainResult 中已有的全部期望 SQL 都能被正确解析")
    void allExplainResultCases() {
        DefaultSqlParser parser = SqlParserFixture.parser()
                .tenantIds(1540616714700591104L)
                .ignoreTableAlias("temp")
                .ignoreTableNamePrefix("act_")
                .build();

        List<String> failures = new ArrayList<>();
        Map<String, String> explainResult = ExplainResult.getExplainResult();
        explainResult.forEach((inputSql, expectedSql) -> {
            String actualSql;
            try {
                actualSql = parser.setTenantParameter(inputSql);
            } catch (Exception e) {
                failures.add("SQL 解析抛异常: " + e + "\n原SQL:\n" + inputSql);
                return;
            }
            if (!SqlParserFixture.collapseWhitespace(expectedSql)
                    .equals(SqlParserFixture.collapseWhitespace(actualSql))) {
                failures.add("期望SQL与实际不一致\n原SQL:\n" + inputSql + "\n期望SQL:\n" + expectedSql
                        + "\n实际SQL:\n" + actualSql);
            }
        });

        assertTrue(explainResult.size() >= 50, "ExplainResult 数据量异常，实际: " + explainResult.size());
        assertTrue(failures.isEmpty(), () -> "共 " + failures.size() + " 条用例不符合预期：\n\n"
                + String.join("\n\n----------\n\n", failures));
    }
}
