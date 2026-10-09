package com.github.osinn.druid.multi.tenant.plugin;

import com.alibaba.druid.DbType;
import com.github.osinn.druid.multi.tenant.plugin.handler.TenantInfoHandler;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 测试用 {@link DefaultSqlParser} 构建器。
 *
 * @author wency_cai
 */
public final class SqlParserFixture {

    private SqlParserFixture() {
    }

    public static Builder parser() {
        return new Builder();
    }

    /**
     * 压缩连续空白、去掉首尾空白（保留大小写）。
     */
    public static String collapseWhitespace(String sql) {
        if (sql == null) {
            return null;
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 归一化 SQL：压缩连续空白、去掉首尾空白并统一小写。
     * <p>
     * druid 输出的 SQL 会带格式化（换行 + 制表符缩进），测试里只关心语义，
     * 因此统一归一化后再比较，避免格式化差异造成误报。
     */
    public static String normalize(String sql) {
        String collapsed = collapseWhitespace(sql);
        return collapsed == null ? null : collapsed.toLowerCase();
    }

    public static final class Builder {

        private final List<Object> tenantIds = new ArrayList<>();
        private final List<String> ignoreTableName = new ArrayList<>();
        private final List<String> ignoreMatchTableAlias = new ArrayList<>();
        private final List<String> ignoreTableNamePrefix = new ArrayList<>();
        private final List<String> ignoreDynamicDatasource = new ArrayList<>();

        private String tenantIdColumn = "tenant_id";
        private DbType dbType = DbType.mysql;
        private String currentDatasource;
        private String customizeParserResult;
        private String beforeSql;
        private String afterSql;
        private boolean skipParser;

        public Builder tenantIds(Object... ids) {
            this.tenantIds.clear();
            this.tenantIds.addAll(Arrays.asList(ids));
            return this;
        }

        public Builder tenantIdColumn(String column) {
            this.tenantIdColumn = column;
            return this;
        }

        public Builder ignoreTableName(String... names) {
            this.ignoreTableName.clear();
            this.ignoreTableName.addAll(Arrays.asList(names));
            return this;
        }

        public Builder ignoreTableAlias(String... aliases) {
            this.ignoreMatchTableAlias.clear();
            this.ignoreMatchTableAlias.addAll(Arrays.asList(aliases));
            return this;
        }

        public Builder ignoreTableNamePrefix(String... prefixes) {
            this.ignoreTableNamePrefix.clear();
            this.ignoreTableNamePrefix.addAll(Arrays.asList(prefixes));
            return this;
        }

        public Builder ignoreDynamicDatasource(String... names) {
            this.ignoreDynamicDatasource.clear();
            this.ignoreDynamicDatasource.addAll(Arrays.asList(names));
            return this;
        }

        public Builder currentDatasource(String name) {
            this.currentDatasource = name;
            return this;
        }

        public Builder dbType(DbType type) {
            this.dbType = type;
            return this;
        }

        public Builder customizeParser(String sql) {
            this.customizeParserResult = sql;
            return this;
        }

        public Builder before(String sql) {
            this.beforeSql = sql;
            return this;
        }

        public Builder after(String sql) {
            this.afterSql = sql;
            return this;
        }

        public Builder skipParser(boolean skip) {
            this.skipParser = skip;
            return this;
        }

        public DefaultSqlParser build() {
            DefaultSqlParser parser = new DefaultSqlParser();
            parser.setTenantService(new ITenantService() {
                @Override
                public List<Object> getTenantIds() {
                    return new ArrayList<>(tenantIds);
                }

                @Override
                public String ignoreDynamicDatasource() {
                    return currentDatasource;
                }

                @Override
                public String customizeParser(String originalSQL, Object paramTenantId) {
                    return customizeParserResult;
                }

                @Override
                public String before(String originalSQL, Object paramTenantId) {
                    return beforeSql;
                }

                @Override
                public String after(String newSQL, Object paramTenantId) {
                    return afterSql;
                }

                @Override
                public boolean skipParser() {
                    return skipParser;
                }
            });
            parser.setTenantInfoHandler(new TenantInfoHandler() {
                @Override
                public DbType getDbType() {
                    return dbType;
                }

                @Override
                public List<Object> getTenantIds() {
                    return new ArrayList<>(tenantIds);
                }

                @Override
                public List<String> ignoreTableName() {
                    return new ArrayList<>(ignoreTableName);
                }

                @Override
                public List<String> ignoreMatchTableAlias() {
                    return new ArrayList<>(ignoreMatchTableAlias);
                }

                @Override
                public List<String> ignoreTableNamePrefix() {
                    return new ArrayList<>(ignoreTableNamePrefix);
                }

                @Override
                public String getTenantIdColumn() {
                    return tenantIdColumn;
                }

                @Override
                public List<String> ignoreDynamicDatasource() {
                    return new ArrayList<>(ignoreDynamicDatasource);
                }
            });
            return parser;
        }
    }
}
