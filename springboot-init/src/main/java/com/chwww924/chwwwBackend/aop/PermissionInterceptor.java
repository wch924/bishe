package com.chwww924.chwwwBackend.aop;

import com.chwww924.chwwwBackend.annotation.RequirePermission;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.service.RbacService;
import com.chwww924.chwwwBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限校验 AOP
 * 基于RBAC的细粒度权限控制
 */
@Slf4j
@Aspect
@Component
public class PermissionInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private RbacService rbacService;

    /**
     * 执行权限拦截
     *
     * @param joinPoint 切点
     * @param requirePermission 权限注解
     * @return 方法执行结果
     */
    @Around("@annotation(requirePermission)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String[] permissionKeys = requirePermission.value();
        RequirePermission.LogicalType logical = requirePermission.logical();

        // 如果没有指定权限，直接放行
        if (permissionKeys == null || permissionKeys.length == 0) {
            return joinPoint.proceed();
        }

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long userId = loginUser.getId();

        // 超级管理员拥有所有权限，直接放行
        if (rbacService.isSuperAdmin(userId)) {
            return joinPoint.proceed();
        }

        // 根据逻辑关系判断权限
        boolean hasPermission = false;
        Set<String> permissionSet = new HashSet<>(Arrays.asList(permissionKeys));

        // 特殊处理：对于 user:read:self 权限，如果用户是查看自己的信息，直接放行
        // 这样可以避免循环依赖（获取角色需要权限，但权限检查需要角色）
        if (permissionSet.contains("user:read:self")) {
            // 检查是否是查看自己的信息
            // 1. 从请求参数中获取 userId（@RequestParam）
            String userIdParam = request.getParameter("userId");
            if (userIdParam != null) {
                try {
                    Long targetUserId = Long.parseLong(userIdParam);
                    if (targetUserId.equals(userId)) {
                        // 查看自己的信息，直接放行
                        hasPermission = true;
                    }
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
            
            // 2. 从方法参数中获取 userId（方法参数）
            if (!hasPermission) {
                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    if (arg instanceof Long) {
                        Long targetUserId = (Long) arg;
                        if (targetUserId != null && targetUserId.equals(userId)) {
                            // 查看自己的信息，直接放行
                            hasPermission = true;
                            break;
                        }
                    }
                }
            }
            
            // 如果是查看自己的信息，已经放行，否则继续检查权限
            if (!hasPermission && logical == RequirePermission.LogicalType.OR) {
                // OR关系：如果 user:read:self 不满足，检查是否有其他权限
                permissionSet.remove("user:read:self");
                if (!permissionSet.isEmpty()) {
                    hasPermission = rbacService.hasAnyPermission(userId, permissionSet);
                }
            }
        }
        
        // 如果没有通过特殊处理，使用常规权限检查
        if (!hasPermission) {
            // 获取用户的所有权限键值，用于调试
            Set<String> userPermissionKeys = rbacService.getUserPermissionKeys(userId);
            log.info("用户ID: {}, 需要的权限: {}, 用户拥有的权限: {}", userId, permissionSet, userPermissionKeys);
            
            if (logical == RequirePermission.LogicalType.AND) {
                // AND关系：需要拥有所有权限
                hasPermission = rbacService.hasAllPermissions(userId, permissionSet);
            } else {
                // OR关系：只需拥有其中一个权限
                hasPermission = rbacService.hasAnyPermission(userId, permissionSet);
            }
            
            log.info("权限检查结果: {}", hasPermission);
        }

        if (!hasPermission) {
            Set<String> userPermissionKeys = rbacService.getUserPermissionKeys(userId);
            log.error("权限不足 - 用户ID: {}, 需要的权限: {}, 用户拥有的权限: {}", userId, permissionSet, userPermissionKeys);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足");
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

