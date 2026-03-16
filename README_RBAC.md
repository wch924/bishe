# 🎯 RBAC 权限系统完整实施指南

## 📚 文档索引

本项目已实现完整的 RBAC（基于角色的访问控制）权限系统，所有相关文档和代码已准备就绪。

### 后端文档
📁 位置：`springboot-init/`

| 文档 | 说明 | 路径 |
|------|------|------|
| **实施指南** | RBAC 系统的详细实现说明 | `RBAC_IMPLEMENTATION_GUIDE.md` |
| **测试指南** | 接口测试和前端对接步骤 | `RBAC_TEST_GUIDE.md` |
| **快速测试** | 快速测试步骤清单 | `test-rbac.md` |

### 前端文档
📁 位置：`yuoj-fron/`

| 文档 | 说明 | 路径 |
|------|------|------|
| **前端对接指南** | 完整的前端集成步骤 | `FRONTEND_INTEGRATION_GUIDE.md` |

### 示例代码
📁 位置：`yuoj-fron/src-examples/`

| 类型 | 文件 | 说明 |
|------|------|------|
| **Store** | `store/rbac.ts` | 权限管理 Store（Pinia） |
| **指令** | `directives/permission.ts` | v-permission 权限指令 |
| **Composable** | `composables/usePermission.ts` | 权限检查 Hook |
| **路由守卫** | `router/guards/permission.ts` | 路由权限守卫 |
| **路由配置** | `router/routes-rbac.ts` | RBAC 路由示例 |
| **页面示例** | `views/admin/RoleManagement.vue` | 角色管理页面完整示例 |

### 工具脚本
📁 位置：`yuoj-fron/`

| 脚本 | 说明 |
|------|------|
| `generate-api.bat` | 一键生成前端 API 代码（Windows） |
| `package.json.example` | npm scripts 配置示例 |

---

## 🚀 快速开始

### 第一步：执行数据库脚本

```sql
-- 1. 执行数据库迁移
source E:/Project/OJ_front/springboot-init/sql/rbac_migration.sql

-- 2. 执行初始化数据
source E:/Project/OJ_front/springboot-init/sql/rbac_init_data.sql
```

**详细步骤请参考：** `springboot-init/test-rbac.md`

### 第二步：启动后端服务

```bash
cd E:/Project/OJ_front/springboot-init
.\mvnw.cmd spring-boot:run
```

访问 Swagger 文档：http://localhost:8121/api/doc.html

### 第三步：测试接口

1. 登录（admin/12345678）
2. 获取当前用户权限：`GET /api/rbac/current/permissions`
3. 查看角色列表：`GET /api/role/list`
4. 查看权限列表：`GET /api/permission/list`

**详细测试步骤请参考：** `springboot-init/RBAC_TEST_GUIDE.md`

### 第四步：生成前端代码

```bash
cd E:/Project/OJ_front/yuoj-fron

# 方法 1：双击运行批处理脚本
# generate-api.bat

# 方法 2：使用命令行
npx --yes openapi-typescript-codegen --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios
```

### 第五步：前端对接

**详细对接步骤请参考：** `yuoj-fron/FRONTEND_INTEGRATION_GUIDE.md`

核心步骤：
1. 复制 `src-examples` 中的文件到 `src` 目录
2. 注册权限指令
3. 配置路由守卫
4. 登录后加载权限
5. 在组件中使用权限控制

---

## 📦 系统架构

### 数据库表结构

```
oj_db
├── roles                 # 角色表（3个系统角色）
├── permissions           # 权限表（21个原子权限）
├── user_roles           # 用户-角色关联表（多对多）
├── role_permissions     # 角色-权限关联表（多对多）
└── user                 # 用户表（新增 coach_id 字段）
```

### 后端架构

```
com.chwww924.chwwwBackend
├── controller/
│   ├── RoleController.java           # 角色管理接口
│   ├── PermissionController.java     # 权限管理接口
│   ├── RbacController.java          # RBAC 核心接口
│   └── UserController.java          # 用户接口（新增教练功能）
├── service/
│   ├── RbacService.java             # RBAC 核心服务
│   ├── RoleService.java             # 角色服务
│   └── PermissionService.java       # 权限服务
├── mapper/
│   ├── RoleMapper.java
│   ├── PermissionMapper.java
│   ├── UserRoleMapper.java
│   └── RolePermissionMapper.java
├── annotation/
│   └── RequirePermission.java       # 权限注解
└── aop/
    └── PermissionInterceptor.java   # 权限拦截器
```

### 前端架构

```
src/
├── store/
│   └── rbac.ts                      # 权限 Store
├── directives/
│   └── permission.ts                # 权限指令
├── composables/
│   └── usePermission.ts             # 权限 Hook
├── router/
│   ├── routes-rbac.ts              # RBAC 路由
│   └── guards/
│       └── permission.ts           # 路由守卫
└── views/
    └── admin/
        ├── RoleManagement.vue      # 角色管理
        ├── PermissionManagement.vue # 权限管理
        └── AssignRoles.vue         # 分配角色
```

---

## 🎭 角色与权限定义

### 三个系统角色

| 角色 | 角色标识 | 说明 |
|------|----------|------|
| 超级管理员 | `super_admin` | 拥有所有权限，可管理角色、权限和用户 |
| 教练员 | `coach` | 可查看和管理绑定的普通用户 |
| 普通用户 | `user` | 只能查看和编辑自己的个人资料 |

### 权限列表（21个）

**用户管理权限（9个）：**
- `user:read:self` - 查看自己的资料
- `user:update:self` - 编辑自己的资料
- `user:read:coach` - 查看教练资料
- `user:read:all` - 查看所有用户
- `user:create` - 创建用户
- `user:update:any` - 编辑任意用户
- `user:delete` - 删除用户
- `user:read:assigned` - 查看绑定用户（教练）
- `user:update:assigned` - 编辑绑定用户（教练）

**角色管理权限（4个）：**
- `role:read` - 查看角色
- `role:create` - 创建角色
- `role:update` - 编辑角色
- `role:delete` - 删除角色

**权限管理权限（4个）：**
- `permission:read` - 查看权限
- `permission:create` - 创建权限
- `permission:update` - 编辑权限
- `permission:delete` - 删除权限

**分配权限（4个）：**
- `user:assign:role` - 为用户分配角色
- `user:remove:role` - 移除用户角色
- `role:assign:permission` - 为角色分配权限
- `role:remove:permission` - 移除角色权限

---

## 💡 核心功能

### ✅ 已实现功能

**权限管理：**
- [x] 基于注解的权限控制（`@RequirePermission`）
- [x] 支持 AND/OR 逻辑的多权限校验
- [x] 超级管理员自动拥有所有权限
- [x] 权限拦截器自动校验
- [x] 前端权限指令（`v-permission`）
- [x] 路由级别的权限守卫

**角色管理：**
- [x] 角色 CRUD 操作
- [x] 系统角色保护（不可删除/修改关键字段）
- [x] 为用户分配/移除角色
- [x] 为角色分配/移除权限
- [x] 查询用户的所有角色
- [x] 查询角色的所有权限

**教练功能：**
- [x] 用户绑定教练（通过 `user.coach_id`）
- [x] 教练查看绑定的用户列表
- [x] 教练管理绑定用户的权限
- [x] 普通用户查看自己的教练信息

**安全机制：**
- [x] 超级管理员不能为自己分配/移除角色
- [x] 系统内置角色受保护
- [x] 事务保证数据一致性
- [x] 登录态验证
- [x] 权限不足统一错误处理

---

## 📖 使用示例

### 后端使用

```java
// 在 Controller 方法上添加权限注解
@PostMapping("/add")
@RequirePermission("role:create")
public BaseResponse<Long> addRole(@RequestBody Role role) {
    // 只有拥有 role:create 权限的用户才能访问
}

// 多权限 OR 逻辑
@GetMapping("/list")
@RequirePermission(value = {"user:read:all", "user:read:self"}, 
                   logical = RequirePermission.LogicalType.OR)
public BaseResponse<List<User>> listUsers() {
    // 拥有任一权限即可访问
}
```

### 前端使用

```vue
<template>
  <!-- 权限指令 -->
  <el-button v-permission="'user:create'">
    创建用户
  </el-button>

  <!-- 编程式检查 -->
  <el-button v-if="canManageUsers" @click="manage">
    管理用户
  </el-button>
</template>

<script setup>
import { usePermission } from '@/composables/usePermission';

const { hasPermission, canManageUsers } = usePermission();

function handleOperation() {
  if (!hasPermission('user:update:any')) {
    ElMessage.error('权限不足');
    return;
  }
  // 执行操作...
}
</script>
```

---

## 🔧 配置说明

### 后端配置

**数据库连接：** `springboot-init/src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oj_db
    username: root
    password: your_password
```

**API 文档：** `springboot-init/src/main/resources/application.yml`
```yaml
knife4j:
  enable: true
  openapi:
    title: "接口文档"
    version: 1.0
```

### 前端配置

**API 基础地址：** `yuoj-fron/src/main.ts`
```typescript
OpenAPI.BASE = 'http://localhost:8121/api';
OpenAPI.WITH_CREDENTIALS = true;
```

**环境变量：** `yuoj-fron/.env`
```env
VITE_API_BASE_URL=http://localhost:8121/api
```

---

## 📝 开发清单

### 后端开发
- [x] 数据库表设计
- [x] 实体类和 DTO
- [x] Mapper 层（MyBatis Plus）
- [x] Service 层（RBAC 核心逻辑）
- [x] Controller 层（RESTful API）
- [x] 权限注解和拦截器
- [x] 初始化数据脚本
- [x] API 文档（Knife4j）

### 前端开发
- [x] API 代码生成工具
- [x] 权限 Store（Pinia）
- [x] 权限指令（Vue Directive）
- [x] 权限 Hook（Composable）
- [x] 路由守卫
- [x] 示例页面（角色管理）
- [x] 完整文档

### 测试
- [x] 数据库测试
- [x] 后端接口测试
- [x] 权限拦截测试
- [x] 教练功能测试
- [ ] 前端集成测试（待用户执行）
- [ ] 端到端测试（待用户执行）

---

## 🎓 学习资源

### 推荐阅读顺序

1. **快速开始** → `springboot-init/test-rbac.md`
2. **理解实现** → `springboot-init/RBAC_IMPLEMENTATION_GUIDE.md`
3. **接口测试** → `springboot-init/RBAC_TEST_GUIDE.md`
4. **前端对接** → `yuoj-fron/FRONTEND_INTEGRATION_GUIDE.md`
5. **查看示例** → `yuoj-fron/src-examples/`

### API 文档

- **Knife4j 文档**: http://localhost:8121/api/doc.html
- **OpenAPI JSON**: http://localhost:8121/api/v2/api-docs

---

## 🐛 常见问题

### 数据库问题
**Q: 执行 SQL 脚本时报错**
- 检查数据库连接
- 检查表是否已存在
- 参考 `springboot-init/test-rbac.md` 中的解决方案

### 后端问题
**Q: 权限拦截不生效**
- 检查是否添加了 `@RequirePermission` 注解
- 检查用户是否已分配角色
- 检查角色是否已分配权限

### 前端问题
**Q: 生成的 API 代码找不到**
- 确保后端服务正在运行
- 检查 `generated` 目录
- 重新运行生成命令

**详细问题解决请参考各文档的"常见问题"章节**

---

## 📞 技术支持

如遇到问题：

1. **查阅文档** - 本 README 索引的所有文档
2. **检查示例** - `yuoj-fron/src-examples/` 中的完整示例
3. **查看日志** - 后端控制台和浏览器 DevTools
4. **测试接口** - 使用 Knife4j 文档测试

---

## 🎉 总结

✅ **RBAC 权限系统已完全实现并准备就绪！**

包含：
- 完整的后端实现（数据库 + API + 权限控制）
- 完整的前端示例（Store + 指令 + 守卫 + 页面）
- 详细的文档（实施 + 测试 + 对接）
- 实用的工具（API 生成脚本）

**现在可以：**
1. 启动后端服务
2. 执行数据库脚本
3. 测试 API 接口
4. 生成前端代码
5. 开始前端开发

祝您使用愉快！ 🚀







