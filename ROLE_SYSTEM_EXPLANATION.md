# 角色系统说明文档

## 📋 问题背景

系统中存在**两套角色系统**，这可能会造成混淆。本文档解释它们的关系和使用场景。

---

## 🔄 两套角色系统

### 1. **旧的角色系统（userRole）**

**位置**：`User` 表的 `userRole` 字段

**值**：
- `user` - 普通用户
- `admin` - 管理员
- `ban` - 封号

**用途**：
- ✅ **向后兼容**：保留原有系统的简单权限控制
- ✅ **快速筛选**：用于简单的用户状态管理
- ✅ **权限检查**：通过 `@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)` 进行粗粒度权限控制

**特点**：
- 每个用户只能有一个角色
- 角色是字符串枚举，不可扩展
- 权限控制简单，只有"是/否"两种状态

---

### 2. **新的RBAC角色系统（roles表）**

**位置**：`roles` 表和 `user_roles` 关联表

**角色**：
- `SuperAdmin` (super_admin) - 超级管理员
- `Coach` (coach) - 教练员
- `User` (user) - 普通用户

**用途**：
- ✅ **细粒度权限控制**：通过 `@RequirePermission` 进行精确权限管理
- ✅ **灵活扩展**：可以动态创建新角色和权限
- ✅ **多角色支持**：一个用户可以拥有多个角色
- ✅ **权限组合**：支持 AND/OR 逻辑组合

**特点**：
- 每个用户可以拥有多个角色
- 角色和权限可以动态管理
- 支持复杂的权限控制逻辑

---

## 🎯 为什么有两套系统？

### 历史原因

1. **原有系统**：项目最初使用简单的 `userRole` 字段进行权限控制
2. **RBAC升级**：后来引入RBAC系统以实现更灵活的权限管理
3. **向后兼容**：为了不影响现有功能，保留了两套系统

### 当前状态

- **旧系统（userRole）**：主要用于**向后兼容**和**简单筛选**
- **新系统（RBAC）**：用于**细粒度权限控制**和**未来扩展**

---

## 📊 表格中的显示

在用户管理页面的表格中，你会看到两列：

| 列名 | 数据来源 | 说明 |
|-----|---------|------|
| **角色** | `user.userRole` | 旧的系统角色（user/admin/ban） |
| **RBAC角色** | `user.roles[]` | 新的RBAC角色列表（SuperAdmin, Coach, User等） |

---

## 🔍 搜索功能改进

### 之前的搜索

搜索下拉框只支持旧的枚举值：
- ❌ `user` - 普通用户
- ❌ `admin` - 管理员
- ❌ `ban` - 封号

### 现在的搜索

现在支持两种搜索方式：

1. **RBAC角色搜索**（推荐）
   - 下拉框显示：`超级管理员`、`教练员`、`普通用户`
   - 搜索拥有指定RBAC角色的用户
   - 支持多角色用户

2. **系统角色搜索**（旧）
   - 下拉框显示：`普通用户`、`管理员`、`封号`
   - 搜索旧的 `userRole` 字段
   - 用于向后兼容

---

## 💡 使用建议

### 对于新功能

✅ **优先使用RBAC系统**：
- 使用 `@RequirePermission` 进行权限控制
- 通过 `RbacService` 检查用户角色和权限
- 在搜索时使用 RBAC 角色下拉框

### 对于旧功能

⚠️ **保持兼容**：
- 旧的 `@AuthCheck` 仍然有效
- `userRole` 字段仍然可以用于简单筛选
- 但建议逐步迁移到RBAC系统

---

## 🔄 迁移建议

### 短期方案（当前）

- ✅ 保留两套系统
- ✅ 新功能使用RBAC系统
- ✅ 旧功能保持兼容

### 长期方案（未来）

可以考虑：

1. **数据迁移**：
   ```sql
   -- 将 userRole='admin' 的用户自动分配 SuperAdmin 角色
   INSERT INTO user_roles (user_id, role_id, assigned_by)
   SELECT id, 1, 1 FROM user WHERE userRole = 'admin';
   ```

2. **代码统一**：
   - 逐步将 `@AuthCheck` 替换为 `@RequirePermission`
   - 统一使用RBAC系统进行权限控制

3. **字段废弃**：
   - 在文档中标记 `userRole` 为"已废弃"
   - 但保留字段以兼容旧数据

---

## 📝 代码示例

### 旧系统权限检查

```java
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<Long> addUser(...) {
    // 只有 userRole='admin' 的用户可以访问
}
```

### 新系统权限检查

```java
@RequirePermission(value = {"user:create"}, type = LogicalType.AND)
public BaseResponse<Long> addUser(...) {
    // 拥有 'user:create' 权限的用户可以访问
    // SuperAdmin 自动拥有所有权限
}
```

### 前端搜索

```typescript
// RBAC角色搜索（推荐）
searchForm.rbacRoleId = 1; // SuperAdmin

// 系统角色搜索（旧）
searchForm.userRole = 'admin';
```

---

## ⚠️ 注意事项

1. **两套系统独立**：
   - `userRole` 和 RBAC 角色是独立的
   - 修改一个不会影响另一个

2. **权限检查优先级**：
   - `@RequirePermission` 优先于 `@AuthCheck`
   - SuperAdmin 拥有所有权限

3. **数据一致性**：
   - 建议保持两套系统的数据一致
   - 例如：`userRole='admin'` 的用户应该分配 `SuperAdmin` 角色

---

## 🎉 总结

- ✅ **旧系统（userRole）**：简单、向后兼容
- ✅ **新系统（RBAC）**：灵活、可扩展
- ✅ **搜索功能**：现在支持两种搜索方式
- ✅ **建议**：新功能优先使用RBAC系统

---

**文档更新时间**：2025-11-18  
**版本**：v1.0.0




