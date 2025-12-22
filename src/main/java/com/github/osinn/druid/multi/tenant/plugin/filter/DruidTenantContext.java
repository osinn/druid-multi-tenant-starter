package com.github.osinn.druid.multi.tenant.plugin.filter;

import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 重新封装skipParser 以支持 Aop
 *
 * @author KisChang
 */
@Slf4j
@Component
public abstract class DruidTenantContext implements ITenantService {

    @Override
    public boolean skipParser() {
        if (isIgnoreTenantId()) {
            return true;
        }
        return false;
    }

    // 支持Aop方式忽略租户
    private static final ThreadLocal<Boolean> IGNORE_TENANT_ID =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** 开启忽略租户 */
    public static void ignoreTenantId() {
        IGNORE_TENANT_ID.set(Boolean.TRUE);
    }

    /** 是否忽略租户 */
    public static boolean isIgnoreTenantId() {
        return Boolean.TRUE.equals(IGNORE_TENANT_ID.get());
    }

    /** 清理（必须在 finally 调用） */
    public static void clear() {
        IGNORE_TENANT_ID.remove();
    }
}