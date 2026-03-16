# RBAC 权限系统 - 快速启动指南

## 🚀 第一步：准备数据库

```bash
# 1. 连接到 MySQL
mysql -u root -p

# 2. 执行迁移脚本（创建表）
mysql> source E:/Project/OJ_front/springboot-init/sql/rbac_migration.sql

# 3. 执行初始化数据（插入角色和权限）
mysql> source E:/Project/OJ_front/springboot-init/sql/rbac_init_data.sql

# 4. 验证数据
mysql> use oj_db;
mysql> SELECT COUNT(*) FROM roles;        # 应该返回 3
mysql> SELECT COUNT(*) FROM permissions;  # 应该返回 21
```

---

## ⚙️ 第二步：启动后端服务

### 方法 1：使用 IDEA
1. 打开 `springboot-init/src/main/java/com/chwww924/chwwwBackend/MainApplication.java`
2. 点击运行按钮
3. 等待启动完成

### 方法 2：使用 Maven 命令
```bash
cd E:/Project/OJ_front/springboot-init
.\mvnw.cmd spring-boot:run
```

### 验证服务启动
浏览器访问: http://localhost:8121/api/doc.html

---

## 🎨 第三步：启动前端服务

```bash
cd E:/Project/OJ_front/yuoj-fron
npm run dev
```

浏览器访问: http://localhost:8080

---

## ✅ 第四步：测试功能

### 1. 登录测试
- 访问前端登录页面
- 使用管理员账号登录：`admin` / `12345678`
- 登录成功后会自动加载权限

### 2. 查看权限
访问测试页面: http://localhost:8080/rbac/test

页面会显示：
- ✅ 当前用户的所有权限（SuperAdmin 有 21 个权限）
- ✅ 根据权限显示/隐藏的按钮（测试 v-permission 指令）
- ✅ 角色列表（点击"加载角色"按钮）
- ✅ 权限列表（点击"加载权限列表"按钮）

### 3. 测试权限控制
在测试页面中：
- **绿色按钮** - 只有拥有特定权限才能看到
- **SuperAdmin** 登录后应该能看到所有按钮
- 可以尝试登出后再登录普通用户，查看按钮显示差异

---

## 📝 已实现的功能

### 后端
- ✅ RBAC 数据库表（roles, permissions, user_roles, role_permissions）
- ✅ 用户表新增 coach_id 字段
- ✅ 角色管理接口（/api/role/*）
- ✅ 权限管理接口（/api/permission/*）
- ✅ RBAC 核心接口（/api/rbac/*）
- ✅ 用户-教练绑定接口（/api/user/bindCoach, /api/user/unbindCoach）
- ✅ 教练查看绑定用户接口（/api/user/coach/assignedUsers）
- ✅ 普通用户查看教练接口（/api/user/myCoach）
- ✅ 权限注解 @RequirePermission
- ✅ 权限拦截器 PermissionInterceptor

### 前端
- ✅ RBAC Store（src/store/rbac.ts）
- ✅ 权限工具函数（src/utils/permission.ts）
- ✅ 权限指令 v-permission（src/directives/permission.ts）
- ✅ 登录后自动加载权限
- ✅ 登出时清空权限
- ✅ RBAC 测试页面（src/views/admin/RbacTestView.vue）

---

## 🎯 权限指令使用示例

```vue
<template>
  <!-- 单个权限 -->
  <a-button v-permission="'user:create'">
    创建用户
  </a-button>

  <!-- 多个权限（OR 逻辑） -->
  <a-button v-permission="['role:create', 'role:update']">
    管理角色
  </a-button>

  <!-- 多个权限（AND 逻辑） -->
  <a-button v-permission.and="['user:create', 'user:delete']">
    高级操作
  </a-button>
</template>
```

---

## 🔧 编程式权限检查

```typescript
import { hasPermission, hasAnyPermission } from '@/utils/permission';

// 检查单个权限
if (hasPermission('user:create')) {
  // 执行操作
}

// 检查多个权限（OR）
if (hasAnyPermission(['user:create', 'user:update'])) {
  // 执行操作
}
```

---

## 📊 系统角色和权限

### 三个系统角色

| 角色 | 角色标识 | 权限数量 | 说明 |
|------|----------|----------|------|
| 超级管理员 | `super_admin` | 21个 | 拥有所有权限 |
| 教练员 | `coach` | 4个 | 可管理绑定的用户 |
| 普通用户 | `user` | 3个 | 只能管理自己 |

### 权限列表（21个）

**用户管理（9个）：**
- user:read:self, user:update:self, user:read:coach
- user:read:all, user:create, user:update:any, user:delete
- user:read:assigned, user:update:assigned

**角色管理（4个）：**
- role:read, role:create, role:update, role:delete

**权限管理（4个）：**
- permission:read, permission:create, permission:update, permission:delete

**分配权限（4个）：**
- user:assign:role, user:remove:role
- role:assign:permission, role:remove:permission

---

## 🧪 完整测试流程

### 1. 测试SuperAdmin权限
```bash
# 登录: admin / 12345678
# 访问: http://localhost:8080/rbac/test
# 应该看到: 21个权限，所有按钮都显示
```

### 2. 创建测试教练用户
在 Knife4j 文档中（http://localhost:8121/api/doc.html）：

```json
POST /api/user/add
{
  "userAccount": "coach001",
  "userName": "测试教练",
  "userPassword": "12345678"
}

# 记录返回的用户ID（例如：1001）

POST /api/rbac/user/assignRoles
{
  "userId": 1001,
  "roleIds": [2]
}
```

### 3. 创建测试普通用户
```json
POST /api/user/add
{
  "userAccount": "user001",
  "userName": "测试用户",
  "userPassword": "12345678"
}

# 记录返回的用户ID（例如：1002）

POST /api/rbac/user/assignRoles
{
  "userId": 1002,
  "roleIds": [3]
}
```

### 4. 绑定用户到教练
```
POST /api/user/bindCoach?userId=1002&coachId=1001
```

### 5. 测试教练权限
```bash
# 登出后用教练账号登录: coach001 / 12345678
# 访问测试页面，应该只看到4个权限
# 测试 GET /api/user/coach/assignedUsers 应该返回 user001
```

### 6. 测试普通用户权限
```bash
# 登出后用普通用户登录: user001 / 12345678
# 访问测试页面，应该只看到3个权限
# 测试 GET /api/user/myCoach 应该返回 coach001 的信息
```

---

## ✅ 验证清单

- [ ] 数据库脚本执行成功
- [ ] 后端服务启动成功
- [ ] 前端服务启动成功
- [ ] 管理员登录后能看到21个权限
- [ ] 测试页面的按钮根据权限显示/隐藏
- [ ] 能成功加载角色列表（3个角色）
- [ ] 能成功加载权限列表（21个权限）
- [ ] 创建教练用户成功
- [ ] 创建普通用户成功
- [ ] 绑定用户到教练成功
- [ ] 教练能查看绑定的用户
- [ ] 普通用户能查看自己的教练

---

## 🎉 成功！

如果以上测试都通过，说明 RBAC 权限系统已经**完全正常工作**！

### 下一步可以做：
1. 根据业务需求创建更多页面
2. 添加角色管理界面（CRUD）
3. 添加权限管理界面（CRUD）
4. 添加用户-角色分配界面
5. 根据需要调整权限粒度

---

## ❓ 常见问题

**Q: 登录后看不到权限？**
- 检查浏览器控制台是否有错误
- 检查后端日志是否正常
- 尝试刷新页面

**Q: 按钮显示不正确？**
- 检查 v-permission 指令是否正确注册
- 检查权限标识是否正确
- 在控制台输入 `this.$store.state.rbac.permissions` 查看权限

**Q: 接口调用失败？**
- 检查后端服务是否运行
- 检查 Cookie 是否正常携带
- 检查 OpenAPI.WITH_CREDENTIALS 是否设置为 true

---

**祝使用愉快！** 🚀







