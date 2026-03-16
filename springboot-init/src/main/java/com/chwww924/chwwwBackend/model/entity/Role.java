package com.chwww924.chwwwBackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色表
 * @TableName roles
 */
@TableName(value = "roles")
@Data
public class Role implements Serializable {
    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称（如SuperAdmin, Coach, User）
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色标识（如super_admin, coach, user）
     */
    @TableField("role_key")
    private String roleKey;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否系统角色（1-系统内置不可删除，0-自定义可删除）
     */
    @TableField("is_system")
    private Integer isSystem;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

