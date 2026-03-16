package com.chwww924.chwwwBackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chwww924.chwwwBackend.model.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID获取该用户的所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.id, p.permission_name AS permissionName, p.permission_key AS permissionKey, " +
            "p.resource_type AS resourceType, p.action, p.description, p.createTime, p.updateTime, p.isDelete " +
            "FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.isDelete = 0")
    List<Permission> selectPermissionsByUserId(Long userId);

    /**
     * 根据角色ID获取该角色的所有权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.id, p.permission_name AS permissionName, p.permission_key AS permissionKey, " +
            "p.resource_type AS resourceType, p.action, p.description, p.createTime, p.updateTime, p.isDelete " +
            "FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.isDelete = 0")
    List<Permission> selectPermissionsByRoleId(Long roleId);
}

