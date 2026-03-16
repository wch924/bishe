# 教练员功能实现文档

## ✅ 已完成的功能

### A. 用户绑定逻辑

#### 1. 查询"无主"用户
- **接口**: `GET /api/coach/users/search`
- **权限**: `user:read:assigned`
- **功能**: 搜索 `coach_id` 为 `null` 的用户
- **参数**: `userAccount`（可选）、`userName`（可选）
- **返回**: 用户列表（UserVO）

#### 2. 绑定用户
- **接口**: `POST /api/coach/users/bind`
- **权限**: `user:update:assigned`
- **功能**: 将用户的 `coach_id` 更新为当前教练的 ID
- **参数**: `studentId`（必填）
- **校验**: 
  - 检查用户是否存在
  - 检查用户是否已绑定其他教练（防止并发冲突）
- **返回**: 操作结果（Boolean）

#### 3. 查看"我的学员"
- **接口**: `GET /api/coach/my-students`
- **权限**: `user:read:assigned`
- **功能**: 返回 `coach_id` 等于当前教练 ID 的用户列表
- **返回**: 用户列表（UserVO）

#### 4. 移除用户（解绑）
- **接口**: `POST /api/coach/users/unbind`
- **权限**: `user:update:assigned`
- **功能**: 将目标用户的 `coach_id` 重置为 `null`
- **参数**: `studentId`（必填）
- **校验**: 
  - 检查用户是否属于当前教练
  - 级联删除：自动从该教练的所有分组中移除该学员
- **返回**: 操作结果（Boolean）

---

### B. 分组管理

#### 1. 数据库表结构

**Groups 表**:
```sql
CREATE TABLE groups (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    coach_id bigint NOT NULL,
    name varchar(100) NOT NULL,
    description varchar(500) NULL,
    createTime datetime DEFAULT CURRENT_TIMESTAMP,
    updateTime datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    isDelete tinyint DEFAULT 0,
    FOREIGN KEY (coach_id) REFERENCES user(id) ON DELETE CASCADE
);
```

**GroupMembers 表**:
```sql
CREATE TABLE group_members (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    group_id bigint NOT NULL,
    student_id bigint NOT NULL,
    createTime datetime DEFAULT CURRENT_TIMESTAMP,
    updateTime datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_group_student (group_id, student_id),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES user(id) ON DELETE CASCADE
);
```

#### 2. 分组 CRUD 操作

**创建分组**:
- **接口**: `POST /api/coach/groups`
- **权限**: `user:read:assigned`
- **参数**: `name`（必填）、`description`（可选）
- **功能**: 创建新分组，自动设置 `coach_id` 为当前教练 ID

**查询我的分组列表**:
- **接口**: `GET /api/coach/groups`
- **权限**: `user:read:assigned`
- **功能**: 返回当前教练创建的分组列表，包含每个组的成员数量
- **返回**: 分组列表（Group），每个分组包含 `memberCount` 字段

**修改分组**:
- **接口**: `PUT /api/coach/groups`
- **权限**: `user:read:assigned`
- **参数**: `id`（必填）、`name`（可选）、`description`（可选）
- **校验**: 必须校验该分组是否属于当前教练

**删除分组**:
- **接口**: `DELETE /api/coach/groups/{id}`
- **权限**: `user:read:assigned`
- **功能**: 删除分组，级联删除 `GroupMembers` 中的关联记录
- **校验**: 必须校验该分组是否属于当前教练

#### 3. 组成员管理

**向分组添加成员**:
- **接口**: `POST /api/coach/groups/{groupId}/members`
- **权限**: `user:read:assigned`
- **参数**: `studentIds`（数组，支持批量添加）
- **核心校验**:
  1. 校验 `groupId` 是否属于当前教练
  2. 遍历 `studentIds`，严格校验每个学生是否已经绑定在当前教练名下
  3. 严禁将非自己名下的学生加入分组
- **返回**: 操作结果（Boolean）

**从分组移除成员**:
- **接口**: `DELETE /api/coach/groups/{groupId}/members/{studentId}`
- **权限**: `user:read:assigned`
- **功能**: 从该分组中移除指定学生
- **校验**: 校验分组是否属于当前教练

**查询分组成员列表**:
- **接口**: `GET /api/coach/groups/{groupId}/members`
- **权限**: `user:read:assigned`
- **功能**: 返回该分组下的所有学生详细信息
- **校验**: 校验分组是否属于当前教练

---

## 🔒 权限控制

所有接口都使用 `@RequirePermission("user:read:assigned")` 或 `@RequirePermission("user:update:assigned")` 进行权限控制。

**权限定义**（已在 `rbac_init_data.sql` 中定义）:
- `user:read:assigned` - 教练可以查看绑定的普通用户列表
- `user:update:assigned` - 教练可以编辑绑定的普通用户信息

---

## ⚠️ 异常处理

### 1. 权限不足
- **错误码**: `NO_AUTH_ERROR` (403)
- **场景**: 
  - 尝试操作不属于自己的分组
  - 尝试添加不属于自己的学生到分组

### 2. 参数错误
- **错误码**: `PARAMS_ERROR` (400)
- **场景**:
  - 必填参数缺失
  - 用户已绑定其他教练
  - 该用户不是您的学员

### 3. 资源不存在
- **错误码**: `NOT_FOUND_ERROR` (404)
- **场景**:
  - 用户不存在
  - 分组不存在

---

## 📋 API 接口列表

### 用户绑定相关

| 方法 | 路径 | 权限 | 说明 |
|-----|------|------|------|
| GET | `/api/coach/users/search` | `user:read:assigned` | 搜索无主用户 |
| POST | `/api/coach/users/bind` | `user:update:assigned` | 绑定用户 |
| GET | `/api/coach/my-students` | `user:read:assigned` | 查看我的学员 |
| POST | `/api/coach/users/unbind` | `user:update:assigned` | 解绑用户 |

### 分组管理相关

| 方法 | 路径 | 权限 | 说明 |
|-----|------|------|------|
| POST | `/api/coach/groups` | `user:read:assigned` | 创建分组 |
| GET | `/api/coach/groups` | `user:read:assigned` | 查询我的分组列表 |
| PUT | `/api/coach/groups` | `user:read:assigned` | 修改分组 |
| DELETE | `/api/coach/groups/{id}` | `user:read:assigned` | 删除分组 |

### 组成员管理相关

| 方法 | 路径 | 权限 | 说明 |
|-----|------|------|------|
| POST | `/api/coach/groups/{groupId}/members` | `user:read:assigned` | 向分组添加成员 |
| DELETE | `/api/coach/groups/{groupId}/members/{studentId}` | `user:read:assigned` | 从分组移除成员 |
| GET | `/api/coach/groups/{groupId}/members` | `user:read:assigned` | 查询分组成员列表 |

---

## 🗂️ 文件结构

### 数据库
- `springboot-init/sql/coach_groups.sql` - 分组表结构

### 实体类
- `model/entity/Group.java` - 分组实体
- `model/entity/GroupMember.java` - 分组-成员关联实体

### DTO
- `model/dto/coach/GroupAddRequest.java` - 创建分组请求
- `model/dto/coach/GroupUpdateRequest.java` - 更新分组请求
- `model/dto/coach/GroupMemberAddRequest.java` - 添加组成员请求
- `model/dto/coach/UserSearchRequest.java` - 搜索用户请求

### Mapper
- `mapper/GroupMapper.java` - 分组 Mapper
- `mapper/GroupMemberMapper.java` - 分组-成员关联 Mapper

### Service
- `service/CoachService.java` - 教练员服务接口
- `service/impl/CoachServiceImpl.java` - 教练员服务实现

### Controller
- `controller/CoachController.java` - 教练员控制器

---

## 🎯 核心业务逻辑

### 1. 用户绑定逻辑

**绑定用户流程**:
1. 检查用户是否存在
2. 检查用户是否已绑定其他教练（`coach_id` 是否为 `null`）
3. 更新用户的 `coach_id` 为当前教练 ID

**解绑用户流程**:
1. 检查用户是否属于当前教练
2. 将用户的 `coach_id` 设为 `null`
3. 级联删除：从该教练的所有分组中移除该学员

### 2. 分组管理逻辑

**创建分组**:
- 自动设置 `coach_id` 为当前教练 ID
- 分组名称必填

**查询分组列表**:
- 只返回当前教练创建的分组
- 自动计算每个分组的成员数量

**操作分组**:
- 所有操作前都校验分组是否属于当前教练
- 删除分组时，级联删除 `GroupMembers` 中的关联记录

### 3. 组成员管理逻辑

**添加成员**:
1. 校验分组是否属于当前教练
2. 遍历 `studentIds`，严格校验每个学生是否已经绑定在当前教练名下
3. 严禁将非自己名下的学生加入分组
4. 检查成员是否已存在，避免重复添加

**移除成员**:
- 校验分组是否属于当前教练
- 从 `GroupMembers` 表中删除关联记录

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
- [ ] 查询我的分组列表（应该只返回自己创建的分组）
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

1. **数据安全**:
   - 教练只能查看和操作自己的学员和分组
   - 所有操作都进行权限校验

2. **级联删除**:
   - 解绑用户时，自动从所有分组中移除
   - 删除分组时，自动删除成员关联

3. **并发控制**:
   - 绑定用户时，再次检查用户是否已绑定，防止并发冲突

4. **权限控制**:
   - 所有接口都使用 RBAC 权限控制
   - 确保只有教练员角色可以访问

---

**文档更新时间**：2025-11-18  
**版本**：v1.0.0




