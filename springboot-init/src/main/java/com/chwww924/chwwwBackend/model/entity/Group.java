package com.chwww924.chwwwBackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 教练分组实体
 */
@Data
@TableName("`groups`")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 教练ID（创建者）
     */
    @TableField("coach_id")
    private Long coachId;

    /**
     * 分组名称
     */
    @TableField("name")
    private String name;

    /**
     * 分组描述
     */
    @TableField("description")
    private String description;

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

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("isDelete")
    private Integer isDelete;

    /**
     * 成员数量（非数据库字段，用于返回给前端）
     */
    @TableField(exist = false)
    private Long memberCount;
}

