package com.chwww924.chwwwBackend.controller;

import com.chwww924.chwwwBackend.annotation.RequirePermission;
import com.chwww924.chwwwBackend.common.BaseResponse;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.common.ResultUtils;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.exception.ThrowUtils;
import com.chwww924.chwwwBackend.model.dto.rbac.AssignPermissionRequest;
import com.chwww924.chwwwBackend.model.dto.rbac.AssignRoleRequest;
import com.chwww924.chwwwBackend.model.entity.Permission;
import com.chwww924.chwwwBackend.model.entity.Role;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.service.RbacService;
import com.chwww924.chwwwBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * RBAC权限管理接口
 */
@RestController
@RequestMapping("/rbac")
@Slf4j
public class RbacController {

    @Resource
    private RbacService rbacService;

    @Resource
    private UserService userService;

    /**
     * 为用户分配角色
     *
     * @param request 分配请求
     * @param httpRequest HTTP请求
     * @return 是否成功
     */
    @PostMapping("/user/assignRoles")
    @RequirePermission("user:assign:role")
    public BaseResponse<Boolean> assignRolesToUser(@RequestBody AssignRoleRequest request,
                                                    HttpServletRequest httpRequest) {
        if (request == null || request.getUserId() == null || request.getRoleIds() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        Long userId = request.getUserId();
        
        // 超级管理员不能为自己分配角色（防止误操作导致权限丢失）
        if (userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能为自己分配角色");
        }
        
        // 检查目标用户是否存在
        User targetUser = userService.getById(userId);
        ThrowUtils.throwIf(targetUser == null, ErrorCode.NOT_FOUND_ERROR, "目标用户不存在");
        
        boolean result = rbacService.assignRolesToUser(userId, request.getRoleIds(), loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param httpRequest HTTP请求
     * @return 是否成功
     */
    @PostMapping("/user/removeRole")
    @RequirePermission("user:remove:role")
    public BaseResponse<Boolean> removeRoleFromUser(@RequestParam Long userId,
                                                     @RequestParam Long roleId,
                                                     HttpServletRequest httpRequest) {
        if (userId == null || roleId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        
        // 不能移除自己的角色
        if (userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能移除自己的角色");
        }
        
        boolean result = rbacService.removeRoleFromUser(userId, roleId);
        return ResultUtils.success(result);
    }

    /**
     * 为角色分配权限
     *
     * @param request 分配请求
     * @return 是否成功
     */
    @PostMapping("/role/assignPermissions")
    @RequirePermission("role:assign:permission")
    public BaseResponse<Boolean> assignPermissionsToRole(@RequestBody AssignPermissionRequest request) {
        if (request == null || request.getRoleId() == null || request.getPermissionIds() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        boolean result = rbacService.assignPermissionsToRole(request.getRoleId(), request.getPermissionIds());
        return ResultUtils.success(result);
    }

    /**
     * 移除角色的权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否成功
     */
    @PostMapping("/role/removePermission")
    @RequirePermission("role:remove:permission")
    public BaseResponse<Boolean> removePermissionFromRole(@RequestParam Long roleId,
                                                          @RequestParam Long permissionId) {
        if (roleId == null || permissionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        boolean result = rbacService.removePermissionFromRole(roleId, permissionId);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户的所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @GetMapping("/user/roles")
    @RequirePermission(value = {"user:read:all", "user:read:self"}, logical = RequirePermission.LogicalType.OR)
    public BaseResponse<List<Role>> getUserRoles(@RequestParam Long userId,
                                                  HttpServletRequest httpRequest) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        
        // 非超级管理员只能查看自己的角色
        if (!rbacService.isSuperAdmin(loginUser.getId()) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能查看自己的角色");
        }
        
        List<Role> roles = rbacService.getUserRoles(userId);
        return ResultUtils.success(roles);
    }

    /**
     * 获取用户的所有权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @GetMapping("/user/permissions")
    @RequirePermission(value = {"user:read:all", "user:read:self"}, logical = RequirePermission.LogicalType.OR)
    public BaseResponse<List<Permission>> getUserPermissions(@RequestParam Long userId,
                                                             HttpServletRequest httpRequest) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        
        // 非超级管理员只能查看自己的权限
        if (!rbacService.isSuperAdmin(loginUser.getId()) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能查看自己的权限");
        }
        
        List<Permission> permissions = rbacService.getUserPermissions(userId);
        return ResultUtils.success(permissions);
    }

    /**
     * 获取当前用户的权限标识集合
     *
     * @param httpRequest HTTP请求
     * @return 权限标识集合
     */
    @GetMapping("/current/permissions")
    public BaseResponse<Set<String>> getCurrentUserPermissions(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Set<String> permissionKeys = rbacService.getUserPermissionKeys(loginUser.getId());
        return ResultUtils.success(permissionKeys);
    }

    /**
     * 获取角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GetMapping("/role/permissions")
    @RequirePermission("permission:read")
    public BaseResponse<List<Permission>> getRolePermissions(@RequestParam Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 通过PermissionMapper查询
        List<Permission> permissions = rbacService.getUserPermissions(roleId);
        return ResultUtils.success(permissions);
    }
}

