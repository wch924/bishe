package com.chwww924.chwwwBackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chwww924.chwwwBackend.model.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户-角色关联Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID删除所有角色关联
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    /**
     * 根据用户ID和角色ID删除关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 根据用户ID查询所有角色ID
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);
}

