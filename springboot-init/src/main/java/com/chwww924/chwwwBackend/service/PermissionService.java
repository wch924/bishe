package com.chwww924.chwwwBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chwww924.chwwwBackend.model.entity.Permission;

/**
 * 权限服务
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 校验权限
     * @param permission 权限
     * @param add 是否为创建校验
     */
    void validPermission(Permission permission, boolean add);

    /**
     * 根据权限标识查询权限
     * @param permissionKey 权限标识
     * @return 权限
     */
    Permission getPermissionByKey(String permissionKey);
}

