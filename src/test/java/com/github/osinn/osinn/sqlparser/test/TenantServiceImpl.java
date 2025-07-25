package com.github.osinn.osinn.sqlparser.test;

import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述
 *
 * @author wency_cai
 */
public class TenantServiceImpl implements ITenantService {

    @Override
    public List<Object> getTenantIds() {
        Long tenantId = 12121L;
        List<Object> tenantIdList = new ArrayList<>();
        tenantIdList.add(tenantId);
        return tenantIdList;
    }

    @Override
    public String ignoreDynamicDatasource() {
        return "demoSource";
    }
}
