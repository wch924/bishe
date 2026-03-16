# 教练员功能实现总结

## ✅ 已完成的工作

### 1. 数据库表结构

**已创建表**：
- ✅ `groups` - 分组表
- ✅ `group_members` - 分组-成员关联表

**表结构验证**：
```sql
SHOW TABLES LIKE '%group%';
-- 结果：groups, group_members ✅
```

---

### 2. 实体类（Entity）

- ✅ `Group.java` - 分组实体
  - 包含 `memberCount` 字段（非数据库字段，用于返回成员数量）
- ✅ `GroupMember.java` - 分组-成员关联实体

---

### 3. DTO 类

- ✅ `GroupAddRequest.java` - 创建分组请求
- ✅ `GroupUpdateRequest.java` - 更新分组请求
- ✅ `GroupMemberAddRequest.java` - 添加组成员请求
- ✅ `UserSearchRequest.java` - 搜索用户请求

---

### 4. Mapper 接口

- ✅ `GroupMapper.java` - 分组 Mapper
  - `selectGroupsByCoachId()` - 查询教练的分组列表
- ✅ `GroupMemberMapper.java` - 分组-成员关联 Mapper
  - `deleteByStudentId()` - 根据学员ID删除所有关联记录（解绑时使用）

---

### 5. Service 层

- ✅ `CoachService.java` - 教练员服务接口
- ✅ `CoachServiceImpl.java` - 教练员服务实现

**实现的功能**：
1. ✅ `searchUnassignedUsers()` - 搜索无主用户
2. ✅ `bindUser()` - 绑定用户
3. ✅ `getMyStudents()` - 查看我的学员
4. ✅ `unbindUser()` - 解绑用户（级联删除分组关联）
5. ✅ `createGroup()` - 创建分组
6. ✅ `getMyGroups()` - 查询我的分组列表（包含成员数量）
7. ✅ `updateGroup()` - 更新分组
8. ✅ `deleteGroup()` - 删除分组（级联删除成员关联）
9. ✅ `addGroupMembers()` - 向分组添加成员（严格校验）
10. ✅ `removeGroupMember()` - 从分组移除成员
11. ✅ `getGroupMembers()` - 查询分组成员列表
12. ✅ `isGroupOwner()` - 校验分组是否属于当前教练

---

### 6. Controller 层

- ✅ `CoachController.java` - 教练员控制器

**实现的接口**：

#### A. 用户绑定相关
- ✅ `GET /api/coach/users/search` - 搜索无主用户
- ✅ `POST /api/coach/users/bind` - 绑定用户
- ✅ `GET /api/coach/my-students` - 查看我的学员
- ✅ `POST /api/coach/users/unbind` - 解绑用户

#### B. 分组管理相关
- ✅ `POST /api/coach/groups` - 创建分组
- ✅ `GET /api/coach/groups` - 查询我的分组列表
- ✅ `PUT /api/coach/groups` - 修改分组
- ✅ `DELETE /api/coach/groups/{id}` - 删除分组

#### C. 组成员管理相关
- ✅ `POST /api/coach/groups/{groupId}/members` - 向分组添加成员
- ✅ `DELETE /api/coach/groups/{groupId}/members/{studentId}` - 从分组移除成员
- ✅ `GET /api/coach/groups/{groupId}/members` - 查询分组成员列表

---

## 🔒 权限控制

所有接口都使用 `@RequirePermission` 注解进行权限控制：

- **用户绑定相关**：
  - `user:read:assigned` - 查看绑定的用户
  - `user:update:assigned` - 编辑绑定的用户

- **分组管理相关**：
  - `user:read:assigned` - 所有分组操作都需要此权限

---

## ⚠️ 安全校验

### 1. 用户绑定校验

- ✅ 绑定用户时，检查用户是否已绑定其他教练
- ✅ 解绑用户时，检查用户是否属于当前教练
- ✅ 解绑时自动级联删除分组关联

### 2. 分组操作校验

- ✅ 所有分组操作都校验分组是否属于当前教练
- ✅ 删除分组时，级联删除成员关联

### 3. 组成员管理校验

- ✅ 添加成员时，严格校验每个学生是否已绑定在当前教练名下
- ✅ 严禁将非自己名下的学生加入分组
- ✅ 操作分组前，校验分组是否属于当前教练

---

## 🎯 核心业务逻辑

### 1. 用户绑定流程

```
搜索无主用户 → 绑定用户 → 查看我的学员 → 解绑用户
     ↓            ↓            ↓              ↓
  coach_id=null  更新coach_id  查询coach_id   重置coach_id
                                    =当前教练    并级联删除
```

### 2. 分组管理流程

```
创建分组 → 查询分组列表 → 添加成员 → 查询成员 → 移除成员 → 删除分组
   ↓          ↓            ↓          ↓          ↓          ↓
设置coach_id  包含成员数量  严格校验   返回成员   删除关联   级联删除
```

### 3. 级联删除逻辑

- **解绑用户**：自动从该教练的所有分组中移除该学员
- **删除分组**：自动删除 `GroupMembers` 中的关联记录

---

## 📋 API 使用示例

### 1. 搜索无主用户

```http
GET /api/coach/users/search?userAccount=test&userName=测试
Authorization: Cookie: JSESSIONID=xxx
```

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "id": 123,
      "userAccount": "test",
      "userName": "测试用户",
      "coachId": null
    }
  ]
}
```

### 2. 绑定用户

```http
POST /api/coach/users/bind?studentId=123
Authorization: Cookie: JSESSIONID=xxx
```

### 3. 创建分组

```http
POST /api/coach/groups
Content-Type: application/json
Authorization: Cookie: JSESSIONID=xxx

{
  "name": "减脂组",
  "description": "专注于减脂训练的学员"
}
```

### 4. 向分组添加成员

```http
POST /api/coach/groups/1/members
Content-Type: application/json
Authorization: Cookie: JSESSIONID=xxx

{
  "studentIds": [123, 456, 789]
}
```

---

## 🧪 测试建议

### 1. 用户绑定测试

- [ ] 搜索无主用户（应该只返回 `coach_id` 为 `null` 的用户）
- [ ] 绑定用户（应该成功）
- [ ] 尝试绑定已绑定其他教练的用户（应该失败）
- [ ] 查看我的学员列表（应该只返回绑定的用户）
- [ ] 解绑用户（应该成功，并自动从所有分组中移除）

### 2. 分组管理测试

- [ ] 创建分组（应该成功）
- [ ] 查询我的分组列表（应该只返回自己创建的分组，包含成员数量）
- [ ] 修改分组（应该成功）
- [ ] 尝试修改其他教练的分组（应该失败，403）
- [ ] 删除分组（应该成功，并级联删除成员关联）

### 3. 组成员管理测试

- [ ] 向分组添加成员（应该成功）
- [ ] 尝试添加非自己名下的学生（应该失败，400）
- [ ] 查询分组成员列表（应该只返回该分组的成员）
- [ ] 从分组移除成员（应该成功）
- [ ] 解绑用户后，检查是否自动从所有分组中移除

---

## 📝 注意事项

1. **数据安全**：
   - 教练只能查看和操作自己的学员和分组
   - 所有操作都进行权限校验

2. **级联删除**：
   - 解绑用户时，自动从所有分组中移除
   - 删除分组时，自动删除成员关联

3. **并发控制**：
   - 绑定用户时，再次检查用户是否已绑定，防止并发冲突

4. **权限控制**：
   - 所有接口都使用 RBAC 权限控制
   - 确保只有教练员角色可以访问

5. **表名注意**：
   - `groups` 是 MySQL 保留关键字，需要使用反引号包裹

---

## 🎉 完成状态

- ✅ 数据库表结构已创建
- ✅ 实体类已创建
- ✅ DTO 类已创建
- ✅ Mapper 接口已创建
- ✅ Service 层已实现
- ✅ Controller 层已实现
- ✅ 权限控制已添加
- ✅ 异常处理已实现
- ✅ 安全校验已实现

**现在可以**：
1. 重启后端服务
2. 使用教练员账号测试所有接口
3. 验证权限控制和业务逻辑

---

**文档更新时间**：2025-11-18  
**版本**：v1.0.0




