package com.github.osinn.druid.multi.tenant.plugin.support;

import com.github.osinn.druid.multi.tenant.plugin.annotation.IgnoreTenantIdField;
import com.github.osinn.druid.multi.tenant.plugin.context.TenantApplicationContext;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

/**
 * 切面方式实现忽略租户ID
 *
 * @author KisChang
 */
public class IgnoreTenantIdFieldPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method metmethodhod, Class<?> targetClass) {
            // 直接使用spring工具包，来获取method上的注解（会找父类上的注解）
            return AnnotatedElementUtils.hasAnnotation(metmethodhod, IgnoreTenantIdField.class);
        }
    };

    private final Advice advice = (MethodInterceptor) invocation -> {
        try {

            TenantApplicationContext.ignoreTenantId();
            return invocation.proceed();
        } finally {
            TenantApplicationContext.clear();
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
