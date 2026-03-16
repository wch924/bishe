package com.chwww924.chwwwBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.exception.ThrowUtils;
import com.chwww924.chwwwBackend.mapper.PermissionMapper;
import com.chwww924.chwwwBackend.model.entity.Permission;
import com.chwww924.chwwwBackend.service.PermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 权限服务实现
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public void validPermission(Permission permission, boolean add) {
        if (permission == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String permissionName = permission.getPermissionName();
        String permissionKey = permission.getPermissionKey();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(permissionName, permissionKey), ErrorCode.PARAMS_ERROR);
        }

        // 有参数则校验
        if (StringUtils.isNotBlank(permissionName) && permissionName.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限名称过长");
        }
        if (StringUtils.isNotBlank(permissionKey) && permissionKey.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限标识过长");
        }
    }

    @Override
    public Permission getPermissionByKey(String permissionKey) {
        if (StringUtils.isBlank(permissionKey)) {
            return null;
        }
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("permission_key", permissionKey);
        queryWrapper.eq("isDelete", 0);
        return this.getOne(queryWrapper);
    }
}

