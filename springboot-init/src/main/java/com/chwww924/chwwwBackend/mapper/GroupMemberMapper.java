package com.chwww924.chwwwBackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chwww924.chwwwBackend.model.entity.GroupMember;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 分组-成员关联 Mapper
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    /**
     * 根据学员ID删除所有关联记录（解绑时使用）
     */
    @Delete("DELETE FROM group_members WHERE student_id = #{studentId}")
    int deleteByStudentId(@Param("studentId") Long studentId);
}




