package com.chwww924.chwwwBackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限表
 * @TableName permissions
 */
@TableName(value = "permissions")
@Data
public class Permission implements Serializable {
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限标识（如user:read, role:create）
     */
    @TableField("permission_key")
    private String permissionKey;

    /**
     * 资源类型（如user, role, permission）
     */
    @TableField("resource_type")
    private String resourceType;

    /**
     * 操作类型（如read, create, update, delete）
     */
    private String action;

    /**
     * 权限描述
     */
    private String description;

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

