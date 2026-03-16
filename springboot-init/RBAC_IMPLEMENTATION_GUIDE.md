# RBAC权限系统实施指南

## 📋 概述

本项目已成功将原有的简单角色权限系统重构为灵活的 RBAC（Role-Based Access Control）基于角色的访问控制模型，专为运动管理系统设计。

## 🎯 核心特性

### 1. 数据库模型

#### 新增表
- **roles** - 角色表：存储系统中的所有角色
- **permissions** - 权限表：定义原子级权限
- **user_roles** - 用户-角色关联表：多对多关系
- **role_permissions** - 角色-权限关联表：多对多关系

#### 修改表
- **user** - 添加 `coach_id` 字段，支持普通用户与教练的绑定关系

### 2. 默认角色与权限

#### 三个系统角色
1. **超级管理员 (super_admin)**
   - 拥有所有权限
   - 可以CRUD角色和权限
   - 可以为其他用户分配角色
   - 可以CRUD所有用户

2. **教练员 (coach)**
   - 查看和编辑自己的资料
   - 查看和编辑绑定的普通用户

3. **普通用户 (user)**
   - 查看和编辑自己的资料
   - 查看自己教练的公开资料

## 📦 部署步骤

### 第一步：执行数据库迁移

```bash
# 1. 执行RBAC表结构创建
mysql -u root -p oj_db < sql/rbac_migration.sql

# 2. 执行初始化数据（角色和权限）
mysql -u root -p oj_db < sql/rbac_init_data.sql
```

**重要提示**：
- `rbac_migration.sql` 会修改 `user` 表，添加 `coach_id` 字段
- `rbac_init_data.sql` 会自动将现有用户根据 `userRole` 字段迁移到新的RBAC系统

### 第二步：验证数据库

执行以下SQL验证表结构：

```sql
-- 查看新增的表
SHOW TABLES LIKE '%role%';
SHOW TABLES LIKE '%permission%';

-- 查看默认角色
SELECT * FROM roles;

-- 查看默认权限
SELECT * FROM permissions;

-- 查看用户角色分配情况
SELECT u.userName, r.role_name 
FROM user u 
INNER JOIN user_roles ur ON u.id = ur.user_id 
INNER JOIN roles r ON ur.role_id = r.role_id;
```

### 第三步：重启应用

```bash
# Maven项目
mvn clean package
mvn spring-boot:run

# 或直接运行
java -jar target/chwwwBackend-0.0.1-SNAPSHOT.jar
```

## 🔧 使用方法

### 1. 在Controller中使用权限注解

#### 旧方式（已保留，向后兼容）
```java
@PostMapping("/update")
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<Boolean> updateQuestion(...) {
    // 只有管理员可以访问
}
```

#### 新方式（推荐）- 使用 @RequirePermission
```java
// 单个权限
@PostMapping("/add")
@RequirePermission("role:create")
public BaseResponse<Long> addRole(...) {
    // 需要 "role:create" 权限
}

// 多个权限（AND关系）
@PostMapping("/update")
@RequirePermission(value = {"user:read:all", "user:update:any"})
public BaseResponse<Boolean> updateUser(...) {
    // 需要同时拥有两个权限
}

// 多个权限（OR关系）
@GetMapping("/user/roles")
@RequirePermission(
    value = {"user:read:all", "user:read:self"}, 
    logical = RequirePermission.LogicalType.OR
)
public BaseResponse<List<Role>> getUserRoles(...) {
    // 只需拥有其中一个权限
}
```

### 2. API接口说明

#### 角色管理 (/role)
- `POST /role/add` - 创建角色（需要 `role:create` 权限）
- `POST /role/update` - 更新角色（需要 `role:update` 权限）
- `POST /role/delete` - 删除角色（需要 `role:delete` 权限）
- `GET /role/get` - 获取角色详情（需要 `role:read` 权限）
- `GET /role/list` - 获取角色列表（需要 `role:read` 权限）

#### 权限管理 (/permission)
- `POST /permission/add` - 创建权限（需要 `permission:create` 权限）
- `POST /permission/update` - 更新权限（需要 `permission:update` 权限）
- `POST /permission/delete` - 删除权限（需要 `permission:delete` 权限）
- `GET /permission/get` - 获取权限详情（需要 `permission:read` 权限）
- `GET /permission/list` - 获取权限列表（需要 `permission:read` 权限）

#### RBAC管理 (/rbac)
- `POST /rbac/user/assignRoles` - 为用户分配角色
- `POST /rbac/user/removeRole` - 移除用户角色
- `POST /rbac/role/assignPermissions` - 为角色分配权限
- `POST /rbac/role/removePermission` - 移除角色权限
- `GET /rbac/user/roles` - 获取用户的所有角色
- `GET /rbac/user/permissions` - 获取用户的所有权限
- `GET /rbac/current/permissions` - 获取当前用户的权限标识集合

#### 教练-用户管理 (/user)
- `POST /user/bindCoach` - 为用户绑定教练（需要管理员权限）
- `POST /user/unbindCoach` - 解除教练绑定（需要管理员权限）
- `GET /user/coach/assignedUsers` - 教练查看绑定的用户列表
- `GET /user/myCoach` - 普通用户查看自己的教练信息

### 3. 权限标识规范

权限标识格式：`资源:操作[:范围]`

示例：
- `user:read:self` - 读取自己的用户信息
- `user:read:all` - 读取所有用户信息
- `user:update:any` - 更新任意用户信息
- `role:create` - 创建角色
- `permission:delete` - 删除权限

### 4. 在代码中使用RbacService

```java
@Resource
private RbacService rbacService;

// 检查用户是否有某个权限
boolean hasPermission = rbacService.hasPermission(userId, "user:read:all");

// 检查用户是否拥有某个角色
boolean isCoach = rbacService.hasRole(userId, "coach");

// 检查用户是否为超级管理员
boolean isSuperAdmin = rbacService.isSuperAdmin(userId);

// 获取用户的所有权限
List<Permission> permissions = rbacService.getUserPermissions(userId);

// 获取用户的所有角色
List<Role> roles = rbacService.getUserRoles(userId);
```

## 🔄 向后兼容性

### 旧的 @AuthCheck 注解依然有效

原有的用户控制器接口使用的 `@AuthCheck` 注解已保留并继续工作：

```java
@PostMapping("/update")
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<Boolean> updateQuestion(...) {
    // 依然有效
}
```

### 旧的 userRole 字段依然有效

- User表中的 `userRole` 字段保留
- 登录、注册等现有功能不受影响
- 前端已对接的接口（如UserController的登录、注册、个人信息等）完全兼容

## 📝 权限列表

### 用户管理相关
- `user:read:self` - 查看自己的资料
- `user:update:self` - 编辑自己的资料
- `user:read:coach` - 查看教练资料
- `user:read:all` - 查看所有用户
- `user:create` - 创建用户
- `user:update:any` - 编辑任意用户
- `user:delete` - 删除用户
- `user:read:assigned` - 查看绑定用户
- `user:update:assigned` - 编辑绑定用户
- `user:assign:role` - 分配角色
- `user:remove:role` - 移除角色

### 角色管理相关
- `role:read` - 查看角色列表
- `role:create` - 创建角色
- `role:update` - 编辑角色
- `role:delete` - 删除角色
- `role:assign:permission` - 为角色分配权限
- `role:remove:permission` - 移除角色权限

### 权限管理相关
- `permission:read` - 查看权限列表
- `permission:create` - 创建权限
- `permission:update` - 编辑权限
- `permission:delete` - 删除权限

## 🔒 安全注意事项

1. **超级管理员保护**：不能为自己分配/移除角色，防止误操作
2. **系统角色保护**：系统内置角色（is_system=1）不能删除
3. **级联删除**：删除角色会自动删除相关的用户-角色和角色-权限关联
4. **权限继承**：超级管理员自动拥有所有权限，无需单独校验

## 🧪 测试建议

### 1. 测试超级管理员功能
```bash
# 使用admin账号登录
# 测试创建角色
POST /api/role/add

# 测试为用户分配角色
POST /api/rbac/user/assignRoles

# 测试为角色分配权限
POST /api/rbac/role/assignPermissions
```

### 2. 测试教练功能
```bash
# 使用coach账号登录
# 查看绑定的用户
GET /api/user/coach/assignedUsers
```

### 3. 测试普通用户功能
```bash
# 使用user账号登录
# 查看自己的教练
GET /api/user/myCoach

# 查看自己的权限
GET /api/rbac/current/permissions
```

## 📚 扩展功能

### 添加新角色
```sql
-- 1. 创建新角色
INSERT INTO roles (role_name, role_key, description, is_system) 
VALUES ('队医', 'team_doctor', '负责运动员健康管理', 0);

-- 2. 为角色分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE role_key = 'team_doctor'),
    id
FROM permissions 
WHERE permission_key IN ('user:read:assigned', 'user:update:assigned');
```

### 添加新权限
```sql
-- 创建新权限
INSERT INTO permissions (permission_name, permission_key, resource_type, action, description)
VALUES ('查看训练数据', 'training:read', 'training', 'read', '查看运动员训练数据');

-- 为角色分配新权限
INSERT INTO role_permissions (role_id, permission_id)
VALUES (
    (SELECT id FROM roles WHERE role_key = 'coach'),
    (SELECT id FROM permissions WHERE permission_key = 'training:read')
);
```

## ⚠️ 已知问题

1. 数据库字段命名不一致（userRole vs role_key），建议后续统一
2. 需要前端配合实现基于权限的UI显示/隐藏
3. 建议添加权限缓存机制提升性能

## 📞 支持

如有问题，请查看：
- 数据库迁移脚本：`sql/rbac_migration.sql`
- 初始化数据脚本：`sql/rbac_init_data.sql`
- 示例代码：Controller包中的各个控制器

