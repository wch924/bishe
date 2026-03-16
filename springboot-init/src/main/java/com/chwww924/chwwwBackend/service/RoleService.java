package com.chwww924.chwwwBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chwww924.chwwwBackend.model.entity.Role;

/**
 * 角色服务
 */
public interface RoleService extends IService<Role> {

    /**
     * 校验角色
     * @param role 角色
     * @param add 是否为创建校验
     */
    void validRole(Role role, boolean add);

    /**
     * 根据角色标识查询角色
     * @param roleKey 角色标识
     * @return 角色
     */
    Role getRoleByKey(String roleKey);
}

