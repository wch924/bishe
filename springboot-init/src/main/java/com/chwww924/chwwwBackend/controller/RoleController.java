package com.chwww924.chwwwBackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chwww924.chwwwBackend.annotation.RequirePermission;
import com.chwww924.chwwwBackend.common.BaseResponse;
import com.chwww924.chwwwBackend.common.DeleteRequest;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.common.ResultUtils;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.exception.ThrowUtils;
import com.chwww924.chwwwBackend.model.entity.Role;
import com.chwww924.chwwwBackend.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 角色管理接口
 */
@RestController
@RequestMapping("/role")
@Slf4j
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 角色ID
     */
    @PostMapping("/add")
    @RequirePermission("role:create")
    public BaseResponse<Long> addRole(@RequestBody Role role) {
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        roleService.validRole(role, true);
        
        // 检查角色标识是否已存在
        Role existRole = roleService.getRoleByKey(role.getRoleKey());
        if (existRole != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色标识已存在");
        }
        
        boolean result = roleService.save(role);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(role.getId());
    }

    /**
     * 删除角色
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @RequirePermission("role:delete")
    public BaseResponse<Boolean> deleteRole(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        
        // 判断角色是否存在
        Role oldRole = roleService.getById(id);
        ThrowUtils.throwIf(oldRole == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 系统角色不允许删除
        if (oldRole.getIsSystem() != null && oldRole.getIsSystem() == 1) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "系统内置角色不允许删除");
        }
        
        boolean result = roleService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    @PostMapping("/update")
    @RequirePermission("role:update")
    public BaseResponse<Boolean> updateRole(@RequestBody Role role) {
        if (role == null || role.getId() == null || role.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        roleService.validRole(role, false);
        
        // 判断角色是否存在
        Role oldRole = roleService.getById(role.getId());
        ThrowUtils.throwIf(oldRole == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 系统角色的关键字段不允许修改
        if (oldRole.getIsSystem() != null && oldRole.getIsSystem() == 1) {
            if (role.getRoleKey() != null && !role.getRoleKey().equals(oldRole.getRoleKey())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "系统内置角色的标识不允许修改");
            }
        }
        
        boolean result = roleService.updateById(role);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/get")
    @RequirePermission("role:read")
    public BaseResponse<Role> getRoleById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Role role = roleService.getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(role);
    }

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    @GetMapping("/list")
    @RequirePermission("role:read")
    public BaseResponse<List<Role>> listRoles() {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByAsc("id");
        List<Role> roleList = roleService.list(queryWrapper);
        return ResultUtils.success(roleList);
    }
}

