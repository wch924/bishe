# RBAC权限系统数据库迁移脚本
# 用于运动管理系统的角色和权限管理

use oj_db;

-- 1. 修改用户表，添加教练外键
ALTER TABLE user ADD COLUMN coach_id bigint null comment '关联的教练ID（仅普通用户）' AFTER userRole;
ALTER TABLE user ADD INDEX idx_coach_id (coach_id);
ALTER TABLE user ADD CONSTRAINT fk_user_coach FOREIGN KEY (coach_id) REFERENCES user(id) ON DELETE SET NULL;

-- 2. 角色表
CREATE TABLE IF NOT EXISTS roles (
    id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name varchar(50) NOT NULL UNIQUE COMMENT '角色名称（如SuperAdmin, Coach, User）',
    role_key varchar(50) NOT NULL UNIQUE COMMENT '角色标识（如super_admin, coach, user）',
    description varchar(255) NULL COMMENT '角色描述',
    is_system tinyint DEFAULT 1 NOT NULL COMMENT '是否系统角色（1-系统内置不可删除，0-自定义可删除）',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete tinyint DEFAULT 0 NOT NULL COMMENT '是否删除'
) ENGINE=InnoDB COMMENT '角色表' COLLATE = utf8mb4_unicode_ci;

-- 3. 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name varchar(100) NOT NULL UNIQUE COMMENT '权限名称',
    permission_key varchar(100) NOT NULL UNIQUE COMMENT '权限标识（如user:read, role:create）',
    resource_type varchar(50) NULL COMMENT '资源类型（如user, role, permission）',
    action varchar(50) NULL COMMENT '操作类型（如read, create, update, delete）',
    description varchar(255) NULL COMMENT '权限描述',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete tinyint DEFAULT 0 NOT NULL COMMENT '是否删除'
) ENGINE=InnoDB COMMENT '权限表' COLLATE = utf8mb4_unicode_ci;

-- 4. 用户-角色关联表（多对多）
CREATE TABLE IF NOT EXISTS user_roles (
    id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    user_id bigint NOT NULL COMMENT '用户ID',
    role_id bigint NOT NULL COMMENT '角色ID',
    assigned_by bigint NULL COMMENT '分配者用户ID',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT '用户-角色关联表' COLLATE = utf8mb4_unicode_ci;

-- 5. 角色-权限关联表（多对多）
CREATE TABLE IF NOT EXISTS role_permissions (
    id bigint AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    role_id bigint NOT NULL COMMENT '角色ID',
    permission_id bigint NOT NULL COMMENT '权限ID',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT '角色-权限关联表' COLLATE = utf8mb4_unicode_ci;

