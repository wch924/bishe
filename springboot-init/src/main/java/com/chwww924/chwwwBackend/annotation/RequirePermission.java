package com.chwww924.chwwwBackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用于基于RBAC的细粒度权限控制
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限标识（如 "user:read:all"）
     * 支持多个权限，默认为AND关系（需要拥有所有权限）
     */
    String[] value() default {};

    /**
     * 多个权限之间的关系
     * AND: 需要拥有所有权限
     * OR: 只需拥有其中一个权限
     */
    LogicalType logical() default LogicalType.AND;

    /**
     * 逻辑类型枚举
     */
    enum LogicalType {
        AND,  // 与关系
        OR    // 或关系
    }
}

