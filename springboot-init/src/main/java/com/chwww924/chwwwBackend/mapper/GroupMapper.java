package com.chwww924.chwwwBackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chwww924.chwwwBackend.model.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分组 Mapper
 */
@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    /**
     * 查询教练的分组列表
     */
    @Select("SELECT g.* FROM `groups` g " +
            "WHERE g.coach_id = #{coachId} AND g.isDelete = 0 " +
            "ORDER BY g.createTime DESC")
    List<Group> selectGroupsByCoachId(Long coachId);
}

