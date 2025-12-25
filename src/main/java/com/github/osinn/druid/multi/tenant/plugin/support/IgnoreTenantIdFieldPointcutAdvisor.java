package com.github.osinn.druid.multi.tenant.plugin.support;

import com.github.osinn.druid.multi.tenant.plugin.annotation.IgnoreTenantIdField;
import com.github.osinn.druid.multi.tenant.plugin.parser.DefaultSqlParser;
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

    private DefaultSqlParser defaultSqlParser;

    public IgnoreTenantIdFieldPointcutAdvisor(DefaultSqlParser defaultSqlParser) {
        this.defaultSqlParser = defaultSqlParser;
    }

    private final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method metmethodhod, Class<?> targetClass) {
            // 直接使用spring工具包，来获取method上的注解（会找父类上的注解）
            return AnnotatedElementUtils.hasAnnotation(metmethodhod, IgnoreTenantIdField.class);
        }
    };

    private final Advice advice = (MethodInterceptor) invocation -> {
        try {
            defaultSqlParser.threadLocalSkipParserSet();
            return invocation.proceed();
        } finally {
            defaultSqlParser.threadLocalSkipParserClear();
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
