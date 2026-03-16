# 教练员角色权限问题排查

## 🐛 问题描述

账号 `coachUser` 在数据库中是教练员角色，但是在前端中却只有普通用户的权限。

## 🔍 问题排查

### 1. 数据库验证

```sql
SELECT u.id, u.userAccount, u.userRole, 
       GROUP_CONCAT(r.role_name ORDER BY r.id) as rbac_roles, 
       GROUP_CONCAT(r.role_key ORDER BY r.id) as role_keys 
FROM user u 
LEFT JOIN user_roles ur ON u.id = ur.user_id 
LEFT JOIN roles r ON ur.role_id = r.id 
WHERE u.userAccount = 'coachUser' AND u.isDelete = 0 
GROUP BY u.id, u.userAccount, u.userRole;
```

**结果**：
- `userAccount`: coachUser
- `userRole`: user (旧系统角色)
- `rbac_roles`: 教练员
- `role_keys`: coach

**结论**：数据库中的角色分配是正确的 ✅

---

### 2. 前端角色加载流程

#### 步骤1：用户登录
- 调用 `UserControllerService.getLoginUserUsingGet()`
- 返回 `LoginUserVO`，包含用户基本信息

#### 步骤2：加载RBAC权限和角色
- 调用 `RbacControllerService.getCurrentUserPermissionsUsingGet()` 获取权限
- 调用 `RbacControllerService.getUserRolesUsingGet(userId)` 获取角色

#### 步骤3：权限检查
- 使用 `store.state.rbac.roleKeys` 检查角色
- 调用 `checkAcess()` 函数检查权限

---

### 3. 可能的问题点

#### 问题1：接口权限检查

**接口**：`GET /api/rbac/user/roles?userId={userId}`

**权限要求**：
```java
@RequirePermission(value = {"user:read:all", "user:read:self"}, logical = RequirePermission.LogicalType.OR)
```

**教练员权限**：
- ✅ `user:read:self` - 教练员有这个权限
- ✅ `user:read:assigned` - 教练员有这个权限

**结论**：教练员应该可以调用这个接口 ✅

#### 问题2：角色加载时机

**路由守卫逻辑**：
```typescript
// 如果需要的权限是RBAC相关，确保已加载权限和角色
if ([ACCESS_MUNE.USER, ACCESS_MUNE.COACH, ACCESS_MUNE.SUPER_ADMIN].includes(needAcess)) {
    const rbacState = store.state.rbac;
    if (!rbacState.hasLoadedPermissions) {
        await store.dispatch("rbac/fetchCurrentPermissions");
    }
}
```

**问题**：如果 `hasLoadedPermissions` 为 `true`，但角色列表为空，不会重新加载。

**解决方案**：检查角色列表是否为空，如果为空则重新加载。

#### 问题3：权限检查逻辑

**当前逻辑**：
```typescript
if (needAcess === ACCESS_MUNE.COACH) {
    return roleKeys.includes('coach') || isSuperAdmin;
}
```

**问题**：如果 `roleKeys` 为空，即使数据库中有角色，也会返回 `false`。

---

## 🔧 修复方案

### 1. 改进角色加载逻辑

**修改 `store/rbac.ts`**：
- 添加详细的调试日志
- 检查接口返回的数据格式
- 确保角色正确加载到 `roleKeys`

### 2. 改进权限检查逻辑

**修改 `access/index.ts`**：
- 如果角色列表为空，重新加载权限
- 确保在权限检查前角色已加载

### 3. 添加调试日志

**已添加的日志**：
- `[RBAC] 获取角色接口响应:` - 显示接口响应
- `[RBAC] 角色加载成功:` - 显示加载的角色
- `[RBAC] 角色标识列表:` - 显示角色标识列表
- `[权限检查] 需要的权限:` - 显示需要的权限
- `[权限检查] 用户角色列表:` - 显示用户角色列表

---

## 🧪 测试步骤

1. **清除浏览器缓存和本地存储**
2. **使用 coachUser 账号登录**
3. **打开浏览器控制台，查看日志**：
   - 检查是否有 `[RBAC] 角色加载成功:` 日志
   - 检查 `roleKeys` 是否包含 `'coach'`
   - 检查是否有权限检查相关的日志
4. **访问教练员测试页面** (`/test/coach`)
   - 检查是否能够访问
   - 检查控制台是否有错误

---

## 📋 检查清单

- [ ] 数据库中的角色分配正确
- [ ] 后端接口返回正确的角色数据
- [ ] 前端正确调用接口
- [ ] 角色数据正确存储到 Vuex store
- [ ] 权限检查逻辑正确
- [ ] 路由守卫正确加载权限

---

## 🎯 下一步

1. **查看浏览器控制台日志**，确认：
   - 角色接口是否成功调用
   - 返回的数据格式是否正确
   - 角色是否正确加载到 `roleKeys`

2. **如果接口调用失败**，检查：
   - 后端日志，看是否有权限错误
   - 网络请求，看接口返回的状态码

3. **如果角色加载成功但权限检查失败**，检查：
   - `checkAcess` 函数的逻辑
   - `roleKeys` 数组的内容

---

**文档更新时间**：2025-11-18  
**版本**：v1.0.0




