package com.chwww924.chwwwBackend.service;

import com.chwww924.chwwwBackend.model.entity.Permission;
import com.chwww924.chwwwBackend.model.entity.Role;

import java.util.List;
import java.util.Set;

/**
 * RBAC权限服务
 */
public interface RbacService {

    /**
     * 检查用户是否拥有指定权限
     * @param userId 用户ID
     * @param permissionKey 权限标识
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionKey);

    /**
     * 检查用户是否拥有多个权限中的任意一个
     * @param userId 用户ID
     * @param permissionKeys 权限标识集合
     * @return 是否拥有任意一个权限
     */
    boolean hasAnyPermission(Long userId, Set<String> permissionKeys);

    /**
     * 检查用户是否拥有所有指定权限
     * @param userId 用户ID
     * @param permissionKeys 权限标识集合
     * @return 是否拥有所有权限
     */
    boolean hasAllPermissions(Long userId, Set<String> permissionKeys);

    /**
     * 获取用户的所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(Long userId);

    /**
     * 获取用户的所有权限标识
     * @param userId 用户ID
     * @return 权限标识集合
     */
    Set<String> getUserPermissionKeys(Long userId);

    /**
     * 获取用户的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @param assignedBy 分配者ID
     * @return 是否成功
     */
    boolean assignRolesToUser(Long userId, List<Long> roleIds, Long assignedBy);

    /**
     * 移除用户的角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean removeRoleFromUser(Long userId, Long roleId);

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 移除角色的权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean removePermissionFromRole(Long roleId, Long permissionId);

    /**
     * 检查用户是否拥有指定角色
     * @param userId 用户ID
     * @param roleKey 角色标识
     * @return 是否拥有角色
     */
    boolean hasRole(Long userId, String roleKey);

    /**
     * 检查用户是否为超级管理员
     * @param userId 用户ID
     * @return 是否为超级管理员
     */
    boolean isSuperAdmin(Long userId);
}

