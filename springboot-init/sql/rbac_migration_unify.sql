-- ============================================
-- RBAC 系统统一迁移脚本
-- 将旧的 userRole 系统迁移到 RBAC 系统
-- ============================================

-- 1. 数据迁移：将 userRole='admin' 的用户自动分配 SuperAdmin 角色
-- 注意：只迁移还没有分配角色的用户，避免重复分配

INSERT INTO user_roles (user_id, role_id, assigned_by, createTime, updateTime)
SELECT 
    u.id AS user_id,
    1 AS role_id,  -- SuperAdmin 角色ID（根据实际数据调整）
    (SELECT id FROM user WHERE userRole = 'admin' AND isDelete = 0 LIMIT 1) AS assigned_by,  -- 系统分配，使用第一个admin用户
    NOW() AS createTime,
    NOW() AS updateTime
FROM user u
WHERE u.userRole = 'admin'
  AND u.isDelete = 0
  AND NOT EXISTS (
      SELECT 1 
      FROM user_roles ur 
      WHERE ur.user_id = u.id 
        AND ur.role_id = 1
  );

-- 2. 将 userRole='user' 的用户自动分配 User 角色（如果还没有分配）
INSERT INTO user_roles (user_id, role_id, assigned_by, createTime, updateTime)
SELECT 
    u.id AS user_id,
    3 AS role_id,  -- User 角色ID（根据实际数据调整）
    (SELECT id FROM user WHERE userRole = 'admin' AND isDelete = 0 LIMIT 1) AS assigned_by,  -- 系统分配，使用第一个admin用户
    NOW() AS createTime,
    NOW() AS updateTime
FROM user u
WHERE u.userRole = 'user'
  AND u.isDelete = 0
  AND NOT EXISTS (
      SELECT 1 
      FROM user_roles ur 
      WHERE ur.user_id = u.id 
        AND ur.role_id = 3
  );

-- 3. 查询迁移结果统计
SELECT 
    '迁移统计' AS description,
    COUNT(*) AS total_users,
    SUM(CASE WHEN userRole = 'admin' THEN 1 ELSE 0 END) AS admin_users,
    SUM(CASE WHEN userRole = 'user' THEN 1 ELSE 0 END) AS normal_users,
    SUM(CASE WHEN userRole = 'ban' THEN 1 ELSE 0 END) AS banned_users
FROM user
WHERE isDelete = 0;

-- 4. 查询已分配角色的用户统计
SELECT 
    r.role_name,
    COUNT(DISTINCT ur.user_id) AS user_count
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id
WHERE r.isDelete = 0
GROUP BY r.id, r.role_name
ORDER BY r.id;

-- 5. 验证：检查是否有 admin 用户没有 SuperAdmin 角色
SELECT 
    u.id,
    u.userAccount,
    u.userName,
    u.userRole,
    GROUP_CONCAT(r.role_name) AS assigned_roles
FROM user u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.userRole = 'admin'
  AND u.isDelete = 0
GROUP BY u.id, u.userAccount, u.userName, u.userRole
HAVING NOT FIND_IN_SET('超级管理员', assigned_roles) OR assigned_roles IS NULL;

