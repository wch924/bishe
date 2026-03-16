package com.chwww924.chwwwBackend.model.dto.rbac;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分配角色请求
 */
@Data
public class AssignRoleRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    private static final long serialVersionUID = 1L;
}

