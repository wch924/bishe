package com.chwww924.chwwwBackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chwww924.chwwwBackend.annotation.RequirePermission;
import com.chwww924.chwwwBackend.common.BaseResponse;
import com.chwww924.chwwwBackend.common.DeleteRequest;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.common.ResultUtils;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.exception.ThrowUtils;
import com.chwww924.chwwwBackend.model.entity.Permission;
import com.chwww924.chwwwBackend.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 权限管理接口
 */
@RestController
@RequestMapping("/permission")
@Slf4j
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 创建权限
     *
     * @param permission 权限信息
     * @return 权限ID
     */
    @PostMapping("/add")
    @RequirePermission("permission:create")
    public BaseResponse<Long> addPermission(@RequestBody Permission permission) {
        if (permission == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        permissionService.validPermission(permission, true);
        
        // 检查权限标识是否已存在
        Permission existPermission = permissionService.getPermissionByKey(permission.getPermissionKey());
        if (existPermission != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限标识已存在");
        }
        
        boolean result = permissionService.save(permission);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(permission.getId());
    }

    /**
     * 删除权限
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @RequirePermission("permission:delete")
    public BaseResponse<Boolean> deletePermission(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        
        // 判断权限是否存在
        Permission oldPermission = permissionService.getById(id);
        ThrowUtils.throwIf(oldPermission == null, ErrorCode.NOT_FOUND_ERROR);
        
        boolean result = permissionService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新权限
     *
     * @param permission 权限信息
     * @return 是否成功
     */
    @PostMapping("/update")
    @RequirePermission("permission:update")
    public BaseResponse<Boolean> updatePermission(@RequestBody Permission permission) {
        if (permission == null || permission.getId() == null || permission.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        permissionService.validPermission(permission, false);
        
        // 判断权限是否存在
        Permission oldPermission = permissionService.getById(permission.getId());
        ThrowUtils.throwIf(oldPermission == null, ErrorCode.NOT_FOUND_ERROR);
        
        boolean result = permissionService.updateById(permission);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取权限
     *
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/get")
    @RequirePermission("permission:read")
    public BaseResponse<Permission> getPermissionById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Permission permission = permissionService.getById(id);
        if (permission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(permission);
    }

    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    @GetMapping("/list")
    @RequirePermission("permission:read")
    public BaseResponse<List<Permission>> listPermissions() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByAsc("resource_type", "action");
        List<Permission> permissionList = permissionService.list(queryWrapper);
        return ResultUtils.success(permissionList);
    }
}

