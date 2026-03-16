package com.chwww924.chwwwBackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chwww924.chwwwBackend.model.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-权限关联Mapper
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID删除所有权限关联
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);

    /**
     * 根据角色ID和权限ID删除关联
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 影响行数
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /**
     * 根据角色ID查询所有权限ID
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM role_permissions WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(Long roleId);
}

