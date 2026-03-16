package com.chwww924.chwwwBackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 分组-成员关联实体
 */
@Data
@TableName("group_members")
public class GroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分组ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 学员ID
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private Date updateTime;
}




