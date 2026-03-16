# 项目 RBAC 权限系统架构与实现文档

本文档详细说明了本项目前后端如何通过 RBAC (Role-Based Access Control) 模型实现用户鉴权及差异化页面显示。

## 1. 系统架构概览

本项目采用 **前后端分离** 的鉴权模式：
*   **后端**：基于 Spring AOP 和自定义注解实现接口级别的细粒度权限控制。
*   **前端**：基于 Vue Router 守卫、Pinia Store 和自定义指令实现页面/按钮级别的显示控制。
*   **数据模型**：标准的 RBAC 模型（用户-角色-权限），并兼容旧版 `userRole` 字段。

---

## 2. 数据库模型设计

系统主要包含五张核心表来支撑 RBAC 模型：

1.  **`user` (用户表)**
    *   存储用户基本信息。
    *   保留 `userRole` 字段用于向后兼容（`admin`/`user`）。
2.  **`roles` (角色表)**
    *   存储角色定义，如：`SuperAdmin` (超级管理员), `Coach` (教练), `User` (普通用户)。
3.  **`permissions` (权限表)**
    *   存储具体的原子权限，如：`user:create`, `role:read`, `training:upload`。
4.  **`user_roles` (用户-角色关联表)**
    *   实现用户与角色的多对多关联。
5.  **`role_permissions` (角色-权限关联表)**
    *   实现角色与权限的多对多关联。

---

## 3. 后端实现 (鉴权核心)

后端通过 **AOP (面向切面编程)** 统一拦截请求进行鉴权。

### 3.1 自定义权限注解 `@RequirePermission`

用于标记 Controller 接口需要什么权限。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    // 需要的权限标识，如 "user:create"
    String[] value() default {};
    
    // 逻辑关系：AND (必须全有) / OR (拥有其一即可)
    LogicalType logical() default LogicalType.AND;
}
```

### 3.2 权限拦截器 `PermissionInterceptor`

这是鉴权的核心逻辑，执行流程如下：

1.  **拦截请求**：拦截带有 `@RequirePermission` 注解的方法。
2.  **获取用户**：通过 `UserService` 获取当前登录用户。
3.  **超级管理员特权**：检查用户是否为 `SuperAdmin`，如果是则**直接放行**。
4.  **特殊权限处理**：
    *   如果是 `user:read:self`（查看自己），检查请求参数中的 ID 是否与当前用户 ID 一致。一致则放行。
5.  **常规权限检查**：
    *   查询用户拥有的所有权限列表（通过 `User -> UserRoles -> RolePermissions -> Permissions`）。
    *   对比接口要求的权限。
    *   **匹配成功** -> 放行 (`joinPoint.proceed()`)。
    *   **匹配失败** -> 抛出 `AuthorizationException`。

### 3.3 示例代码

```java
@PostMapping("/add")
// 需要 "role:create" 权限才能访问
@RequirePermission("role:create")
public BaseResponse<Long> addRole(@RequestBody Role role) {
    return ResultUtils.success(roleService.save(role));
}
```

---

## 4. 前端实现 (页面显示与路由控制)

前端通过状态管理和路由守卫来实现页面访问控制。

### 4.1 权限状态管理 (`store/rbac.ts`)

使用 Pinia/Vuex 管理用户的权限状态。

*   **State**: `permissions` (权限列表), `roles` (角色对象列表), `roleKeys` (角色标识字符串列表)。
*   **Actions**: `fetchCurrentPermissions`
    *   调用后端 API `GET /api/rbac/current/permissions` 获取当前用户权限。
    *   调用后端 API `GET /api/rbac/user/{id}/roles` 获取当前用户角色。
    *   存储到 Store 中供全局使用。

### 4.2 路由鉴权 (`router/guards/permission.ts`)

在页面跳转前（`beforeEach`）进行检查：

```typescript
router.beforeEach(async (to, from, next) => {
    // 1. 获取目标页面需要的权限 (定义在路由 meta 中)
    const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;

    // 2. 如果不需要登录，直接放行
    if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
        next();
        return;
    }

    // 3. 确保用户已获取最新的 RBAC 权限信息
    // 如果是首次加载或缺少权限数据，触发 store action 拉取
    if (!store.state.rbac.hasLoadedPermissions) {
        await store.dispatch("rbac/fetchCurrentPermissions");
    }

    // 4. 权限检查
    const loginUser = store.state.user.loginUser;
    if (!checkAccess(loginUser, needAccess)) {
        // 无权限，跳转到 401 或 403 页面
        next('/noAuth');
        return;
    }

    // 5. 放行
    next();
});
```

### 4.3 权限检查工具 (`access/checkAccess.ts`)

统一的权限判断逻辑：

1.  **超级管理员**：如果用户拥有 `super_admin` 角色，返回 `true`。
2.  **角色匹配**：
    *   如果页面需 `COACH` 权限 -> 检查用户是否有 `coach` 角色。
    *   如果页面需 `ADMIN` 权限 -> 检查用户是否有 `admin` (旧) 或 `super_admin` (新) 角色。
3.  **兼容性**：同时支持旧的 `userRole` 字段判断和新的 RBAC 角色判断。

### 4.4 页面元素的显隐控制

对于页面内部的按钮或区域，使用：

1.  **自定义指令 `v-permission`**:
    ```html
    <!-- 只有拥有 user:create 权限的用户能看到此按钮 -->
    <button v-permission="'user:create'">创建用户</button>
    ```

2.  **计算属性 (Computed)**:
    ```typescript
    const isCoach = computed(() => store.getters['rbac/isCoach']);
    // 在模板中 user v-if="isCoach"
    ```

---

## 5. 完整交互流程总结

1.  **用户登录**：后端验证账号密码，返回 Token 或 Session。
2.  **前端初始化**：前端获取用户信息，并触发 `rbac/fetchCurrentPermissions` 拉取用户的角色和权限列表。
3.  **页面访问**：
    *   用户点击菜单进入 `/coach/dashboard`。
    *   **路由守卫**拦截：发现该路由 `meta.access` 为 `COACH`。
    *   **权限检查**：检查 Store 中是否包含 `coach` 角色。
    *   **结果**：有则进入页面，无则跳转无权限页。
4.  **数据操作**：
    *   用户点击 "上传训练数据" 按钮。
    *   前端发送请求 `POST /api/training/upload`。
    *   **后端拦截器**：发现接口标记了 `@RequirePermission("training:upload")`。
    *   **后端鉴权**：查询数据库确认用户是否有该权限。
    *   **结果**：有权限则执行业务逻辑，无权限则返回 403 错误。

---

## 6. 优势

*   **安全性**：后端兜底，即使前端被绕过，后端接口依然安全。
*   **灵活性**：通过数据库配置即可修改角色权限，无需重新部署代码。
*   **体验好**：前端根据权限动态显示菜单和按钮，用户只能看到自己能操作的功能。
