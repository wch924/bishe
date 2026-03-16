package com.chwww924.chwwwBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.exception.ThrowUtils;
import com.chwww924.chwwwBackend.mapper.RoleMapper;
import com.chwww924.chwwwBackend.model.entity.Role;
import com.chwww924.chwwwBackend.service.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public void validRole(Role role, boolean add) {
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String roleName = role.getRoleName();
        String roleKey = role.getRoleKey();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(roleName, roleKey), ErrorCode.PARAMS_ERROR);
        }

        // 有参数则校验
        if (StringUtils.isNotBlank(roleName) && roleName.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色名称过长");
        }
        if (StringUtils.isNotBlank(roleKey) && roleKey.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色标识过长");
        }
    }

    @Override
    public Role getRoleByKey(String roleKey) {
        if (StringUtils.isBlank(roleKey)) {
            return null;
        }
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_key", roleKey);
        queryWrapper.eq("isDelete", 0);
        return this.getOne(queryWrapper);
    }
}

