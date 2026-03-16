package com.chwww924.chwwwBackend.service.impl;

import com.chwww924.chwwwBackend.mapper.PermissionMapper;
import com.chwww924.chwwwBackend.mapper.RoleMapper;
import com.chwww924.chwwwBackend.mapper.RolePermissionMapper;
import com.chwww924.chwwwBackend.mapper.UserRoleMapper;
import com.chwww924.chwwwBackend.model.entity.Permission;
import com.chwww924.chwwwBackend.model.entity.Role;
import com.chwww924.chwwwBackend.model.entity.RolePermission;
import com.chwww924.chwwwBackend.model.entity.UserRole;
import com.chwww924.chwwwBackend.service.RbacService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RBAC权限服务实现
 */
@Service
public class RbacServiceImpl implements RbacService {
    
    // 角色ID常量
    private static final Long ROLE_SUPER_ADMIN = 1L;  // SuperAdmin
    private static final Long ROLE_COACH = 2L;         // Coach
    private static final Long ROLE_USER = 3L;          // User

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public boolean hasPermission(Long userId, String permissionKey) {
        if (userId == null || permissionKey == null) {
            return false;
        }
        Set<String> permissionKeys = getUserPermissionKeys(userId);
        return permissionKeys.contains(permissionKey);
    }

    @Override
    public boolean hasAnyPermission(Long userId, Set<String> permissionKeys) {
        if (userId == null || permissionKeys == null || permissionKeys.isEmpty()) {
            return false;
        }
        Set<String> userPermissionKeys = getUserPermissionKeys(userId);
        for (String key : permissionKeys) {
            if (userPermissionKeys.contains(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(Long userId, Set<String> permissionKeys) {
        if (userId == null || permissionKeys == null || permissionKeys.isEmpty()) {
            return false;
        }
        Set<String> userPermissionKeys = getUserPermissionKeys(userId);
        return userPermissionKeys.containsAll(permissionKeys);
    }

    @Override
    public List<Permission> getUserPermissions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return permissionMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public Set<String> getUserPermissionKeys(Long userId) {
        List<Permission> permissions = getUserPermissions(userId);
        return permissions.stream()
                .map(Permission::getPermissionKey)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return roleMapper.selectBatchIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds, Long assignedBy) {
        if (userId == null || roleIds == null || roleIds.isEmpty()) {
            return false;
        }
        
        // 先删除用户现有的所有角色
        userRoleMapper.deleteByUserId(userId);
        
        // 如果分配了教练员角色，移除普通用户角色（因为教练员已包含普通用户的所有权限）
        boolean hasCoachRole = roleIds.contains(ROLE_COACH);
        if (hasCoachRole) {
            roleIds.remove(ROLE_USER);
        }
        
        // 插入新的角色关联
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setAssignedBy(assignedBy);
            userRoleMapper.insert(userRole);
        }
        
        return true;
    }

    @Override
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            return false;
        }
        int result = userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (roleId == null || permissionIds == null || permissionIds.isEmpty()) {
            return false;
        }
        
        // 先删除角色现有的所有权限
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 插入新的权限关联
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
        
        return true;
    }

    @Override
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
        if (roleId == null || permissionId == null) {
            return false;
        }
        int result = rolePermissionMapper.deleteByRoleIdAndPermissionId(roleId, permissionId);
        return result > 0;
    }

    @Override
    public boolean hasRole(Long userId, String roleKey) {
        if (userId == null || roleKey == null) {
            return false;
        }
        List<Role> roles = getUserRoles(userId);
        return roles.stream().anyMatch(role -> roleKey.equals(role.getRoleKey()));
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        return hasRole(userId, "super_admin");
    }
}

