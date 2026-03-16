# RBAC 系统统一迁移指南

## 📋 概述

本文档说明如何将旧的 `userRole` 系统统一迁移到 RBAC 系统，实现代码统一和权限管理的可扩展性。

---

## 🎯 迁移目标

1. ✅ **数据迁移**：将 `userRole='admin'` 的用户自动分配 `SuperAdmin` 角色
2. ✅ **代码统一**：统一使用 RBAC 系统进行权限控制
3. ✅ **前端权限**：根据 RBAC 权限开放不同页面，支持可扩展性

---

## 📝 实施步骤

### 第一步：执行数据迁移

#### 方式1：使用 SQL 脚本（推荐）

```bash
# 连接到数据库
mysql -uroot -p598675126 oj_db

# 执行迁移脚本
source springboot-init/sql/rbac_migration_unify.sql
```

#### 方式2：使用后端 API

```bash
# 执行迁移
POST http://localhost:8121/api/rbac/migration/execute

# 验证迁移结果
POST http://localhost:8121/api/rbac/migration/validate
```

#### 方式3：使用 Java 服务

```java
@Autowired
private RbacMigrationService rbacMigrationService;

// 执行迁移
int migratedCount = rbacMigrationService.migrateUserRoles();

// 验证结果
int unmigratedCount = rbacMigrationService.validateMigration();
```

---

### 第二步：更新前端权限枚举

已更新 `ACCESS_MUNE.ts`：

```typescript
const ACCESS_MUNE = {
    NOT_LOGIN: 'notlogin',
    USER: 'user',              // 普通用户（旧系统，向后兼容）
    ADMIN: 'admin',            // 管理员（旧系统，向后兼容）
    COACH: 'coach',            // 教练员（RBAC系统）✨ 新增
    SUPER_ADMIN: 'super_admin', // 超级管理员（RBAC系统）✨ 新增
};
```

---

### 第三步：使用新的权限控制

#### 在路由中使用

```typescript
// routes.ts
{
  path: "/admin/users",
  name: "用户管理",
  component: UserManagementView,
  meta: {
    access: ACCESS_MUNE.SUPER_ADMIN,  // 使用 RBAC 权限
    hideMenu: false,
  },
},
{
  path: "/coach/dashboard",
  name: "教练工作台",
  component: CoachDashboard,
  meta: {
    access: ACCESS_MUNE.COACH,  // 教练员专用
    hideMenu: true,
  },
},
```

#### 在组件中使用

```vue
<template>
  <div>
    <!-- 使用 v-permission 指令 -->
    <a-button v-permission="'user:create'">创建用户</a-button>
    
    <!-- 使用权限检查函数 -->
    <a-button v-if="hasPermission('user:delete')">删除用户</a-button>
  </div>
</template>

<script setup lang="ts">
import { hasPermission } from '@/utils/permission';
import { useStore } from 'vuex';

const store = useStore();

// 检查是否是超级管理员
const isSuperAdmin = computed(() => store.getters['rbac/isSuperAdmin']);

// 检查是否是教练员
const isCoach = computed(() => store.getters['rbac/isCoach']);

// 检查是否拥有指定角色
const hasCoachRole = computed(() => store.getters['rbac/hasRole']('coach'));
</script>
```

---

## 🔧 权限检查逻辑

### 权限优先级

1. **超级管理员（SuperAdmin）**：拥有所有权限
2. **RBAC 角色**：根据角色和权限进行细粒度控制
3. **旧系统角色（向后兼容）**：`admin` 用户仍然可以访问需要 `ADMIN` 权限的页面

### 权限检查流程

```
用户访问页面
    ↓
检查是否需要登录
    ↓
加载用户信息和 RBAC 权限/角色
    ↓
检查权限（按优先级）：
  1. SuperAdmin → 直接放行
  2. RBAC 角色 → 检查角色和权限
  3. 旧系统角色 → 向后兼容
    ↓
允许/拒绝访问
```

---

## 📊 权限映射关系

| 旧系统 (userRole) | RBAC 角色 | 权限级别 | 说明 |
|------------------|----------|---------|------|
| `admin` | `SuperAdmin` | 最高 | 拥有所有权限 |
| `user` | `User` | 普通 | 基础权限 |
| `ban` | 无 | 封禁 | 无法访问 |

### 新增角色

| RBAC 角色 | 权限级别 | 说明 |
|----------|---------|------|
| `Coach` | 中等 | 教练员专用权限 |
| `SuperAdmin` | 最高 | 系统管理员 |

---

## 🎨 前端权限控制示例

### 1. 路由权限控制

```typescript
// routes.ts
export const routes = [
  {
    path: "/admin",
    meta: {
      access: ACCESS_MUNE.SUPER_ADMIN,  // 只有超级管理员可以访问
    },
  },
  {
    path: "/coach",
    meta: {
      access: ACCESS_MUNE.COACH,  // 只有教练员可以访问
    },
  },
  {
    path: "/user",
    meta: {
      access: ACCESS_MUNE.USER,  // 普通用户也可以访问
    },
  },
];
```

### 2. 组件内权限控制

```vue
<template>
  <div>
    <!-- 方式1：使用 v-permission 指令 -->
    <a-button v-permission="'user:create'">创建用户</a-button>
    
    <!-- 方式2：使用 v-if + 权限检查 -->
    <a-button v-if="canCreateUser">创建用户</a-button>
    
    <!-- 方式3：使用角色检查 -->
    <a-button v-if="isSuperAdmin">系统设置</a-button>
    <a-button v-if="isCoach">管理学员</a-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useStore } from 'vuex';
import { hasPermission } from '@/utils/permission';

const store = useStore();

// 权限检查
const canCreateUser = computed(() => hasPermission('user:create'));

// 角色检查
const isSuperAdmin = computed(() => store.getters['rbac/isSuperAdmin']);
const isCoach = computed(() => store.getters['rbac/isCoach']);
</script>
```

### 3. 菜单权限控制

```vue
<template>
  <a-menu>
    <a-menu-item v-if="isSuperAdmin">
      <router-link to="/admin/users">用户管理</router-link>
    </a-menu-item>
    <a-menu-item v-if="isCoach">
      <router-link to="/coach/students">学员管理</router-link>
    </a-menu-item>
    <a-menu-item>
      <router-link to="/user/profile">个人中心</router-link>
    </a-menu-item>
  </a-menu>
</template>
```

---

## 🔄 迁移检查清单

### 数据迁移

- [ ] 执行 SQL 迁移脚本
- [ ] 验证迁移结果（检查是否有未迁移的用户）
- [ ] 确认所有 `admin` 用户都有 `SuperAdmin` 角色

### 代码更新

- [ ] 更新 `ACCESS_MUNE.ts`（已完成 ✅）
- [ ] 更新 `checkAcess.ts`（已完成 ✅）
- [ ] 更新 `access/index.ts`（已完成 ✅）
- [ ] 更新 `store/rbac.ts`（已完成 ✅）
- [ ] 更新路由配置（使用新的权限枚举）

### 测试验证

- [ ] 测试超级管理员登录和权限
- [ ] 测试教练员登录和权限
- [ ] 测试普通用户登录和权限
- [ ] 测试旧系统 `admin` 用户的向后兼容性

---

## ⚠️ 注意事项

### 1. 向后兼容

- ✅ 旧的 `userRole='admin'` 用户仍然可以访问需要 `ADMIN` 权限的页面
- ✅ 旧的权限检查逻辑仍然有效
- ✅ 建议逐步迁移到 RBAC 系统

### 2. 权限优先级

- **SuperAdmin** 拥有最高权限，可以访问所有页面
- RBAC 角色权限优先于旧系统角色
- 如果用户同时拥有多个角色，取最高权限

### 3. 数据一致性

- 建议保持 `userRole` 和 RBAC 角色的数据一致
- 例如：`userRole='admin'` 的用户应该分配 `SuperAdmin` 角色

---

## 📚 相关文档

- [RBAC 系统实现总结](./IMPLEMENTATION_SUMMARY.md)
- [角色系统说明文档](./ROLE_SYSTEM_EXPLANATION.md)
- [用户管理功能指南](./USER_MANAGEMENT_GUIDE.md)

---

## 🎉 完成状态

- ✅ 数据迁移脚本已创建
- ✅ 后端迁移服务已实现
- ✅ 前端权限枚举已更新
- ✅ 权限检查逻辑已改进
- ✅ RBAC Store 已增强
- ✅ 路由守卫已更新

**下一步**：执行数据迁移，然后测试新的权限系统！

---

**文档更新时间**：2025-11-18  
**版本**：v1.0.0




