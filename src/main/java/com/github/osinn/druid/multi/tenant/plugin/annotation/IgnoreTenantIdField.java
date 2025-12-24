package com.github.osinn.druid.multi.tenant.plugin.annotation;

import java.lang.annotation.*;

/**
 * 忽略多租户字段注解
 *
 * @author wency_cai
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreTenantIdField {
}
