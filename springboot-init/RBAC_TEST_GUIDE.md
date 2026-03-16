# RBAC 权限系统测试与前端对接指南

## 📋 目录
1. [数据库准备](#1-数据库准备)
2. [启动后端服务](#2-启动后端服务)
3. [接口测试](#3-接口测试)
4. [生成前端请求代码](#4-生成前端请求代码)
5. [前端对接](#5-前端对接)

---

## 1. 数据库准备

### 步骤 1.1：执行数据库迁移脚本

确保 MySQL 服务正在运行，然后执行以下 SQL 脚本：

```bash
# 连接到 MySQL
mysql -u root -p

# 执行迁移脚本
mysql> source E:/Project/OJ_front/springboot-init/sql/rbac_migration.sql

# 执行初始化数据脚本
mysql> source E:/Project/OJ_front/springboot-init/sql/rbac_init_data.sql
```

**或者使用 MySQL Workbench / Navicat 等工具：**
1. 打开 `sql/rbac_migration.sql`，执行
2. 打开 `sql/rbac_init_data.sql`，执行

### 步骤 1.2：验证数据库结构

执行以下 SQL 验证表是否创建成功：

```sql
USE oj_db;

-- 检查新表
SHOW TABLES;

-- 应该看到以下新表：
-- roles
-- permissions  
-- user_roles
-- role_permissions

-- 检查 user 表是否添加了 coach_id 字段
DESCRIBE user;

-- 验证初始化数据
SELECT * FROM roles;
SELECT * FROM permissions;
SELECT * FROM role_permissions;
SELECT * FROM user_roles;
```

**预期结果：**
- 3 个角色：SuperAdmin, Coach, User
- 21 个权限
- SuperAdmin 拥有所有权限
- 现有的 admin 用户已自动分配 SuperAdmin 角色
- 现有的 user 用户已自动分配 User 角色

---

## 2. 启动后端服务

### 步骤 2.1：启动 Spring Boot 应用

```bash
cd E:/Project/OJ_front/springboot-init

# 使用 Maven 启动（Windows）
.\mvnw.cmd spring-boot:run

# 或直接在 IDEA 中运行 MainApplication.java
```

### 步骤 2.2：验证服务启动成功

服务启动后，访问以下 URL：

**Knife4j 接口文档：**
```
http://localhost:8121/api/doc.html
```

**Swagger API Docs（用于生成前端代码）：**
```
http://localhost:8121/api/v2/api-docs
```

如果能正常访问，说明服务启动成功！

---

## 3. 接口测试

### 步骤 3.1：登录获取 Session

在 Knife4j 文档页面 (`http://localhost:8121/api/doc.html`)：

1. 找到 **用户接口 > 用户登录**
2. 点击 **调试**
3. 输入测试数据：
   ```json
   {
     "userAccount": "admin",
     "userPassword": "12345678"
   }
   ```
4. 点击 **发送**
5. 如果返回成功，cookie 会自动保存，后续请求会带上 session

### 步骤 3.2：测试 RBAC 核心接口

#### 3.2.1 获取当前用户权限
```
GET /api/rbac/current/permissions
```

**预期返回：**（SuperAdmin 应该拥有所有权限）
```json
{
  "code": 0,
  "data": [
    "user:read:self",
    "user:update:self",
    "user:read:coach",
    "user:read:all",
    "user:create",
    "user:update:any",
    "user:delete",
    "user:read:assigned",
    "user:update:assigned",
    "role:read",
    "role:create",
    "role:update",
    "role:delete",
    "permission:read",
    "permission:create",
    "permission:update",
    "permission:delete",
    "user:assign:role",
    "user:remove:role",
    "role:assign:permission",
    "role:remove:permission"
  ],
  "message": "ok"
}
```

#### 3.2.2 查看所有角色
```
GET /api/role/list
```

**预期返回：**
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "roleName": "超级管理员",
      "roleKey": "super_admin",
      "description": "拥有系统所有权限，可管理角色、权限和用户",
      "isSystem": 1
    },
    {
      "id": 2,
      "roleName": "教练员",
      "roleKey": "coach",
      "description": "可查看和管理绑定的普通用户",
      "isSystem": 1
    },
    {
      "id": 3,
      "roleName": "普通用户",
      "roleKey": "user",
      "description": "只能查看和编辑自己的个人资料",
      "isSystem": 1
    }
  ],
  "message": "ok"
}
```

#### 3.2.3 查看所有权限
```
GET /api/permission/list
```

**预期返回：** 21 个权限的列表

#### 3.2.4 创建测试用户并分配角色

**创建一个教练用户：**
```
POST /api/user/add
```
```json
{
  "userAccount": "coach001",
  "userName": "测试教练",
  "userPassword": "12345678"
}
```

**为用户分配教练角色：**
```
POST /api/rbac/user/assignRoles
```
```json
{
  "userId": <新创建的用户ID>,
  "roleIds": [2]
}
```

**创建一个普通用户：**
```json
{
  "userAccount": "user001",
  "userName": "测试用户",
  "userPassword": "12345678"
}
```

**为用户分配普通用户角色：**
```json
{
  "userId": <新创建的用户ID>,
  "roleIds": [3]
}
```

#### 3.2.5 测试教练-用户绑定

**绑定用户到教练：**
```
POST /api/user/bindCoach?userId=<普通用户ID>&coachId=<教练用户ID>
```

**使用教练账号登录，查看绑定的用户：**
1. 先登出：`POST /api/user/logout`
2. 用教练账号登录：`POST /api/user/login`
3. 查看绑定用户：`GET /api/user/coach/assignedUsers`

**使用普通用户账号登录，查看自己的教练：**
1. 登出后用普通用户登录
2. 查看教练信息：`GET /api/user/myCoach`

### 步骤 3.3：测试权限控制

**用普通用户账号尝试访问需要管理员权限的接口：**

```
GET /api/role/list
```

**预期返回：**
```json
{
  "code": 40101,
  "data": null,
  "message": "权限不足"
}
```

这说明权限拦截正常工作！

---

## 4. 生成前端请求代码

### 步骤 4.1：确保后端服务正在运行

确保后端服务已启动，并且可以访问：
```
http://localhost:8121/api/v2/api-docs
```

### 步骤 4.2：进入前端项目目录

```bash
cd E:/Project/OJ_front/yuoj-fron
```

### 步骤 4.3：安装 openapi-typescript-codegen

```bash
npm install openapi-typescript-codegen --save-dev
```

### 步骤 4.4：生成前端请求代码

```bash
npx openapi-typescript-codegen --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios
```

**或者在 package.json 中添加脚本：**

```json
{
  "scripts": {
    "generate:api": "openapi-typescript-codegen --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios"
  }
}
```

然后执行：
```bash
npm run generate:api
```

### 步骤 4.5：验证生成的代码

生成完成后，检查 `generated` 目录：

```
yuoj-fron/generated/
├── core/
│   ├── ApiError.ts
│   ├── ApiRequestOptions.ts
│   ├── ApiResult.ts
│   ├── CancelablePromise.ts
│   ├── OpenAPI.ts
│   └── request.ts
├── models/
│   ├── Role.ts                      ✨ 新增
│   ├── Permission.ts                ✨ 新增
│   ├── AssignRoleRequest.ts         ✨ 新增
│   ├── AssignPermissionRequest.ts   ✨ 新增
│   ├── BaseResponse_List_Role_.ts   ✨ 新增
│   └── ... (其他模型)
├── services/
│   ├── RoleControllerService.ts     ✨ 新增
│   ├── PermissionControllerService.ts ✨ 新增
│   ├── RbacControllerService.ts     ✨ 新增
│   ├── UserControllerService.ts     (已更新)
│   └── ... (其他服务)
└── index.ts
```

---

## 5. 前端对接

### 步骤 5.1：配置 API 基础 URL

在前端项目中配置 API 地址（通常在 `generated/core/OpenAPI.ts`）：

```typescript
import { OpenAPI } from '@/generated';

// 在应用初始化时配置
OpenAPI.BASE = 'http://localhost:8121/api';
OpenAPI.WITH_CREDENTIALS = true; // 重要：携带 Cookie
```

### 步骤 5.2：创建权限管理 Store

创建 `src/store/rbac.ts`：

```typescript
import { defineStore } from 'pinia';
import { RbacControllerService } from '@/generated';

export const useRbacStore = defineStore('rbac', {
  state: () => ({
    permissions: [] as string[],
    loading: false,
  }),

  actions: {
    /**
     * 获取当前用户权限
     */
    async fetchCurrentPermissions() {
      this.loading = true;
      try {
        const res = await RbacControllerService.getCurrentUserPermissions();
        if (res.code === 0) {
          this.permissions = res.data || [];
        }
      } catch (error) {
        console.error('获取权限失败', error);
      } finally {
        this.loading = false;
      }
    },

    /**
     * 检查是否拥有权限
     */
    hasPermission(permissionKey: string): boolean {
      return this.permissions.includes(permissionKey);
    },

    /**
     * 检查是否拥有任意一个权限
     */
    hasAnyPermission(permissionKeys: string[]): boolean {
      return permissionKeys.some(key => this.permissions.includes(key));
    },

    /**
     * 检查是否拥有所有权限
     */
    hasAllPermissions(permissionKeys: string[]): boolean {
      return permissionKeys.every(key => this.permissions.includes(key));
    },

    /**
     * 清空权限（登出时调用）
     */
    clearPermissions() {
      this.permissions = [];
    },
  },
});
```

### 步骤 5.3：创建权限指令

创建 `src/directives/permission.ts`：

```typescript
import { Directive } from 'vue';
import { useRbacStore } from '@/store/rbac';

/**
 * 权限指令
 * 用法：v-permission="'user:create'"
 * 或：v-permission="['user:create', 'user:update']"
 */
export const permission: Directive = {
  mounted(el, binding) {
    const rbacStore = useRbacStore();
    const value = binding.value;

    if (!value) return;

    let hasPermission = false;

    if (typeof value === 'string') {
      hasPermission = rbacStore.hasPermission(value);
    } else if (Array.isArray(value)) {
      // 默认为 OR 逻辑
      hasPermission = rbacStore.hasAnyPermission(value);
    }

    if (!hasPermission) {
      el.style.display = 'none';
      // 或者直接移除元素
      // el.parentNode?.removeChild(el);
    }
  },
};
```

在 `src/main.ts` 中注册指令：

```typescript
import { createApp } from 'vue';
import App from './App.vue';
import { permission } from './directives/permission';

const app = createApp(App);

// 注册权限指令
app.directive('permission', permission);

app.mount('#app');
```

### 步骤 5.4：在登录后获取权限

修改登录逻辑（例如在 `src/store/user.ts` 或登录组件中）：

```typescript
import { useUserStore } from '@/store/user';
import { useRbacStore } from '@/store/rbac';
import { UserControllerService } from '@/generated';

async function handleLogin(loginForm: any) {
  try {
    // 1. 执行登录
    const res = await UserControllerService.userLogin(loginForm);
    
    if (res.code === 0) {
      // 2. 保存用户信息
      const userStore = useUserStore();
      userStore.setLoginUser(res.data);
      
      // 3. 获取权限信息 ✨ 新增
      const rbacStore = useRbacStore();
      await rbacStore.fetchCurrentPermissions();
      
      // 4. 跳转到首页
      router.push('/');
    }
  } catch (error) {
    console.error('登录失败', error);
  }
}
```

### 步骤 5.5：在页面中使用权限控制

#### 示例 1：按钮权限控制

```vue
<template>
  <div>
    <!-- 只有拥有 user:create 权限的用户才能看到这个按钮 -->
    <el-button 
      v-permission="'user:create'"
      type="primary" 
      @click="handleCreate"
    >
      创建用户
    </el-button>

    <!-- 拥有 user:update:any 或 user:delete 任一权限即可看到 -->
    <el-button 
      v-permission="['user:update:any', 'user:delete']"
      type="danger"
    >
      管理用户
    </el-button>
  </div>
</template>
```

#### 示例 2：编程式权限检查

```vue
<script setup lang="ts">
import { computed } from 'vue';
import { useRbacStore } from '@/store/rbac';

const rbacStore = useRbacStore();

// 检查是否可以创建角色
const canCreateRole = computed(() => {
  return rbacStore.hasPermission('role:create');
});

// 检查是否可以管理用户
const canManageUsers = computed(() => {
  return rbacStore.hasAnyPermission([
    'user:create',
    'user:update:any',
    'user:delete'
  ]);
});

function handleOperation() {
  if (!rbacStore.hasPermission('user:update:any')) {
    ElMessage.error('您没有权限执行此操作');
    return;
  }
  // 执行操作...
}
</script>

<template>
  <div>
    <el-button v-if="canCreateRole" @click="createRole">
      创建角色
    </el-button>
    
    <div v-if="canManageUsers">
      <h3>用户管理面板</h3>
      <!-- 管理界面内容 -->
    </div>
  </div>
</template>
```

#### 示例 3：路由守卫中的权限检查

在 `src/router/index.ts` 中：

```typescript
import { createRouter } from 'vue-router';
import { useRbacStore } from '@/store/rbac';
import { useUserStore } from '@/store/user';

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const rbacStore = useRbacStore();

  // 需要登录的页面
  if (to.meta.requireAuth && !userStore.loginUser) {
    next('/login');
    return;
  }

  // 需要权限的页面
  if (to.meta.permissions) {
    // 确保已加载权限
    if (rbacStore.permissions.length === 0) {
      await rbacStore.fetchCurrentPermissions();
    }

    const requiredPermissions = to.meta.permissions as string[];
    const hasPermission = rbacStore.hasAnyPermission(requiredPermissions);

    if (!hasPermission) {
      ElMessage.error('您没有权限访问此页面');
      next('/403'); // 跳转到无权限页面
      return;
    }
  }

  next();
});

// 路由配置示例
const routes = [
  {
    path: '/admin/roles',
    name: 'RoleManagement',
    component: () => import('@/views/admin/RoleManagement.vue'),
    meta: {
      requireAuth: true,
      permissions: ['role:read', 'role:create'], // 需要任一权限
    },
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: {
      requireAuth: true,
      permissions: ['user:read:all'],
    },
  },
];
```

### 步骤 5.6：创建角色管理页面示例

创建 `src/views/admin/RoleManagement.vue`：

```vue
<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button 
            v-permission="'role:create'"
            type="primary" 
            @click="handleCreate"
          >
            创建角色
          </el-button>
        </div>
      </template>

      <el-table :data="roleList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleKey" label="角色标识" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="系统角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSystem === 1 ? 'success' : 'info'">
              {{ row.isSystem === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              v-permission="'role:update'"
              size="small" 
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              v-permission="'role:delete'"
              v-if="row.isSystem !== 1"
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { RoleControllerService } from '@/generated';
import type { Role } from '@/generated';

const roleList = ref<Role[]>([]);

// 获取角色列表
async function fetchRoles() {
  try {
    const res = await RoleControllerService.listRoles();
    if (res.code === 0) {
      roleList.value = res.data || [];
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败');
  }
}

// 创建角色
function handleCreate() {
  // 打开创建对话框
}

// 编辑角色
function handleEdit(role: Role) {
  // 打开编辑对话框
}

// 删除角色
async function handleDelete(role: Role) {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
      type: 'warning',
    });

    const res = await RoleControllerService.deleteRole({
      id: role.id,
    });

    if (res.code === 0) {
      ElMessage.success('删除成功');
      fetchRoles(); // 刷新列表
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
}

onMounted(() => {
  fetchRoles();
});
</script>
```

---

## 6. 测试清单

### ✅ 后端测试
- [ ] 数据库迁移成功
- [ ] 初始化数据正确
- [ ] 服务启动成功
- [ ] Swagger 文档可访问
- [ ] 登录功能正常
- [ ] 获取当前用户权限接口正常
- [ ] SuperAdmin 拥有所有权限
- [ ] 角色列表接口正常
- [ ] 权限列表接口正常
- [ ] 为用户分配角色功能正常
- [ ] 教练-用户绑定功能正常
- [ ] 权限拦截正常（普通用户访问管理接口被拒绝）

### ✅ 前端测试
- [ ] OpenAPI 代码生成成功
- [ ] 新增的 Service 文件存在
- [ ] 新增的 Model 文件存在
- [ ] 登录后能获取权限
- [ ] v-permission 指令生效
- [ ] 权限控制按钮显示/隐藏正常
- [ ] 路由守卫权限检查正常
- [ ] 角色管理页面正常显示
- [ ] 权限管理页面正常显示

---

## 7. 常见问题

### Q1: 执行 SQL 脚本时报错 "Duplicate column name 'coach_id'"

**原因：** coach_id 字段已存在

**解决：** 检查 User 实体类中是否已有 coachId 字段，如果有，可以跳过 ALTER TABLE 语句

### Q2: 生成前端代码时报错

**可能原因：**
1. 后端服务未启动
2. API 文档地址错误
3. openapi-typescript-codegen 未安装

**解决：**
```bash
# 确认服务运行
curl http://localhost:8121/api/v2/api-docs

# 重新安装工具
npm install openapi-typescript-codegen --save-dev
```

### Q3: 登录后获取权限返回空数组

**原因：** 用户未分配角色

**解决：** 执行 rbac_init_data.sql 中的数据迁移部分，为现有用户分配角色

### Q4: 权限拦截不生效

**检查：**
1. PermissionInterceptor 是否被 Spring 扫描到（应该有 @Component 注解）
2. 方法上是否添加了 @RequirePermission 注解
3. 用户是否已登录
4. 用户是否已分配角色和权限

---

## 8. 下一步计划

完成测试后，可以考虑：

1. **完善前端页面**
   - 角色管理页面（CRUD）
   - 权限管理页面（CRUD）
   - 用户-角色分配页面
   - 角色-权限分配页面

2. **优化用户体验**
   - 权限不足时的友好提示
   - 加载状态优化
   - 错误处理优化

3. **功能扩展**
   - 权限缓存（Redis）
   - 权限变更实时通知
   - 操作日志记录
   - 数据权限控制

---

**祝测试顺利！** 🎉

如有问题，请参考：
- [RBAC_IMPLEMENTATION_GUIDE.md](./RBAC_IMPLEMENTATION_GUIDE.md) - 实现指南
- Knife4j 文档: http://localhost:8121/api/doc.html








