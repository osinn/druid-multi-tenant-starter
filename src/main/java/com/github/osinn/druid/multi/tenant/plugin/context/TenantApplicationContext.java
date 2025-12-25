package com.github.osinn.druid.multi.tenant.plugin.context;

import com.github.osinn.druid.multi.tenant.plugin.service.ITenantService;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author wency_cai
 */
public abstract class TenantApplicationContext implements ApplicationContextAware, ITenantService {

    private static ApplicationContext context;

    private static final ThreadLocal<Boolean> IGNORE_TENANT_ID = ThreadLocal.withInitial(() -> Boolean.FALSE);


    @Override
    public boolean skipParser() {
        return isIgnoreTenantId();
    }

    @Override
    public void threadLocalSkipParserSet() {
        ignoreTenantId();
    }

    @Override
    public void threadLocalSkipParserClear() {
        clear();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    /**
     * 忽略租户
     */
    private void ignoreTenantId() {
        IGNORE_TENANT_ID.set(Boolean.TRUE);
    }

    /**
     * 是否忽略租户
     *
     * @return 返回 true 忽略租户
     */
    private boolean isIgnoreTenantId() {
        return Boolean.TRUE.equals(IGNORE_TENANT_ID.get());
    }

    /**
     * 清理 IGNORE_TENANT_ID 上下文
     */
    private void clear() {
        IGNORE_TENANT_ID.remove();
    }
}