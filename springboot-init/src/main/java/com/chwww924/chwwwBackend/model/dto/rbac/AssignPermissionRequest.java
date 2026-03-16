package com.chwww924.chwwwBackend.model.dto.rbac;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 为角色分配权限请求
 */
@Data
public class AssignPermissionRequest implements Serializable {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    private static final long serialVersionUID = 1L;
}

