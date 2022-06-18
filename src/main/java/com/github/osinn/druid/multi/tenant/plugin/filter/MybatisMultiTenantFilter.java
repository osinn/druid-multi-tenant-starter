package com.github.osinn.druid.multi.tenant.plugin.filter;

import java.util.List;

/**
 * 过滤器实现
 *
 * @author wency_cai
 */
public class MybatisMultiTenantFilter implements MultiTenantFilter {

    /**
     * 忽略表前缀
     */
    private List<String> ignoreTablePrefix;


    public MybatisMultiTenantFilter(List<String> ignoreTablePrefix) {
        this.ignoreTablePrefix = ignoreTablePrefix;
    }


    @Override
    public boolean doTableFilter(String table) {
        for (String tablePrefix : this.ignoreTablePrefix) {
            if (table.contains(tablePrefix)) {
                return false;
            }
        }
        return true;
    }

}
