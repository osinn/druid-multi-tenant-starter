package com.github.osinn.druid.multi.tenant.plugin.filter;

import com.github.osinn.druid.multi.tenant.plugin.annotation.IgnoreTenantIdField;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 自定义实现 pointcut 让 IgnoreTenantIdField 可以写在接口上
 * （类似Mybatis plus、Fenix Jpa等框架也可以使用，不需要 TenantInfoHandler.IGNORE_TENANT_ID_METHODS 去处理了）
 *
 * @author KisChang
 */
@Component
public class IgnoreTenantIdFieldAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            // 直接使用spring工具包，来获取method上的注解（会找父类上的注解）
            return AnnotatedElementUtils.hasAnnotation(method, IgnoreTenantIdField.class);
        }
    };

    private final Advice advice = (MethodInterceptor) invocation -> {
        try {
            DruidTenantContext.ignoreTenantId();
            return invocation.proceed();
        } finally {
            DruidTenantContext.clear();
        }
    };

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
}