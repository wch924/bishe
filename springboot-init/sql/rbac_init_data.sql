# RBAC权限系统初始化数据
# 插入默认角色和权限

use oj_db;

-- ============ 1. 插入默认角色 ============
INSERT INTO roles (id, role_name, role_key, description, is_system) VALUES
(1, '超级管理员', 'super_admin', '拥有系统所有权限，可管理角色、权限和用户', 1),
(2, '教练员', 'coach', '可查看和管理绑定的普通用户', 1),
(3, '普通用户', 'user', '只能查看和编辑自己的个人资料', 1);

-- ============ 2. 插入默认权限 ============

-- 用户管理相关权限
INSERT INTO permissions (permission_name, permission_key, resource_type, action, description) VALUES
-- 用户基础权限
('查看自己的资料', 'user:read:self', 'user', 'read', '用户可以查看自己的个人资料'),
('编辑自己的资料', 'user:update:self', 'user', 'update', '用户可以编辑自己的个人资料'),
('查看教练资料', 'user:read:coach', 'user', 'read', '用户可以查看自己教练的公开资料'),

-- 用户管理权限（管理员）
('查看所有用户', 'user:read:all', 'user', 'read', '查看系统中所有用户信息'),
('创建用户', 'user:create', 'user', 'create', '创建新用户'),
('编辑任意用户', 'user:update:any', 'user', 'update', '编辑任意用户信息'),
('删除用户', 'user:delete', 'user', 'delete', '删除用户'),

-- 教练相关权限
('查看绑定用户', 'user:read:assigned', 'user', 'read', '教练可以查看绑定的普通用户列表'),
('编辑绑定用户', 'user:update:assigned', 'user', 'update', '教练可以编辑绑定的普通用户信息'),

-- 角色管理相关权限
('查看角色列表', 'role:read', 'role', 'read', '查看系统中的角色列表'),
('创建角色', 'role:create', 'role', 'create', '创建新角色'),
('编辑角色', 'role:update', 'role', 'update', '编辑角色信息'),
('删除角色', 'role:delete', 'role', 'delete', '删除角色'),

-- 权限管理相关权限
('查看权限列表', 'permission:read', 'permission', 'read', '查看系统中的权限列表'),
('创建权限', 'permission:create', 'permission', 'create', '创建新权限'),
('编辑权限', 'permission:update', 'permission', 'update', '编辑权限信息'),
('删除权限', 'permission:delete', 'permission', 'delete', '删除权限'),

-- 角色分配相关权限
('分配角色', 'user:assign:role', 'user', 'assign', '为用户分配角色'),
('移除角色', 'user:remove:role', 'user', 'remove', '移除用户的角色'),

-- 权限分配相关权限
('为角色分配权限', 'role:assign:permission', 'role', 'assign', '为角色分配权限'),
('移除角色权限', 'role:remove:permission', 'role', 'remove', '移除角色的权限');

-- ============ 3. 为角色分配权限 ============

-- 3.1 超级管理员 (SuperAdmin) - 拥有所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- 3.2 教练员 (Coach) - 拥有查看和编辑自己及绑定用户的权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE permission_key IN (
    'user:read:self',
    'user:update:self',
    'user:read:assigned',
    'user:update:assigned'
);

-- 3.3 普通用户 (User) - 只能查看和编辑自己的资料，查看教练资料
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE permission_key IN (
    'user:read:self',
    'user:update:self',
    'user:read:coach'
);

-- ============ 4. 数据迁移：将现有用户的userRole映射到新的RBAC系统 ============
-- 注意：这会为所有现有用户根据userRole字段分配相应的角色

-- 为 admin 用户分配超级管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT id, 1 FROM user WHERE userRole = 'admin' AND isDelete = 0
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 为 user 用户分配普通用户角色
INSERT INTO user_roles (user_id, role_id)
SELECT id, 3 FROM user WHERE userRole = 'user' AND isDelete = 0
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 注意：coach 类型的用户需要手动分配，因为原系统没有这个角色
-- 如果有需要，可以先创建 coach 用户，然后执行：
-- INSERT INTO user_roles (user_id, role_id) VALUES (coach_user_id, 2);

