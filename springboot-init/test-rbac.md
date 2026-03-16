# 🚀 RBAC 系统快速测试指南

## 第一步：执行数据库脚本

### 方法 1：使用 MySQL 命令行（推荐）

```powershell
# 1. 打开 PowerShell 或 CMD
# 2. 连接到 MySQL
mysql -u root -p

# 3. 输入密码后，执行以下命令
mysql> source E:/Project/OJ_front/springboot-init/sql/rbac_migration.sql
mysql> source E:/Project/OJ_front/springboot-init/sql/rbac_init_data.sql

# 4. 验证数据
mysql> use oj_db;
mysql> SELECT COUNT(*) FROM roles;        # 应该返回 3
mysql> SELECT COUNT(*) FROM permissions;  # 应该返回 21
mysql> SELECT * FROM roles;
```

### 方法 2：使用 Navicat/MySQL Workbench 等图形化工具

1. 连接到数据库 `oj_db`
2. 打开并执行 `sql/rbac_migration.sql`
3. 打开并执行 `sql/rbac_init_data.sql`
4. 查看表确认数据是否正确

---

## 第二步：启动后端服务

### 方法 1：在 IDEA 中启动

1. 打开 `MainApplication.java`
2. 点击 Run 按钮
3. 等待启动完成

### 方法 2：使用 Maven 命令

```powershell
cd E:\Project\OJ_front\springboot-init
.\mvnw.cmd spring-boot:run
```

### 验证服务启动

打开浏览器访问：
- **Knife4j 文档**: http://localhost:8121/api/doc.html
- **API Docs**: http://localhost:8121/api/v2/api-docs

---

## 第三步：测试核心接口

### 3.1 登录（获取 Session）

在 Knife4j 文档页面：

1. 找到 **用户接口** > **用户登录** (`POST /api/user/login`)
2. 点击 **调试**
3. 输入：
   ```json
   {
     "userAccount": "admin",
     "userPassword": "12345678"
   }
   ```
4. 点击 **发送**
5. 应该看到返回成功

### 3.2 获取当前用户权限

1. 找到 **RBAC权限管理接口** > **获取当前用户的权限标识集合** (`GET /api/rbac/current/permissions`)
2. 点击 **调试** > **发送**
3. 应该看到返回 21 个权限标识

**预期返回示例：**
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

✅ **如果看到这个返回，说明 RBAC 系统工作正常！**

### 3.3 查看角色列表

1. 找到 **角色管理接口** > **获取角色列表** (`GET /api/role/list`)
2. 点击 **发送**
3. 应该看到 3 个角色：SuperAdmin, Coach, User

### 3.4 查看权限列表

1. 找到 **权限管理接口** > **获取权限列表** (`GET /api/permission/list`)
2. 点击 **发送**
3. 应该看到 21 个权限

---

## 第四步：测试教练-用户功能

### 4.1 创建测试教练用户

1. 找到 **用户接口** > **创建用户（仅管理员）** (`POST /api/user/add`)
2. 输入：
   ```json
   {
     "userAccount": "coach001",
     "userName": "测试教练",
     "userPassword": "12345678",
     "userRole": "user"
   }
   ```
3. 记录返回的用户 ID（假设为 `1001`）

### 4.2 为教练分配角色

1. 找到 **RBAC权限管理接口** > **为用户分配角色** (`POST /api/rbac/user/assignRoles`)
2. 输入（将 userId 改为上一步的 ID）：
   ```json
   {
     "userId": 1001,
     "roleIds": [2]
   }
   ```
3. 应该返回成功

### 4.3 创建测试普通用户

1. 再次调用 **创建用户** 接口：
   ```json
   {
     "userAccount": "user001",
     "userName": "测试用户1",
     "userPassword": "12345678",
     "userRole": "user"
   }
   ```
2. 记录用户 ID（假设为 `1002`）

### 4.4 为普通用户分配角色

```json
{
  "userId": 1002,
  "roleIds": [3]
}
```

### 4.5 绑定用户到教练

1. 找到 **用户接口** > **为普通用户绑定教练** (`POST /api/user/bindCoach`)
2. 输入参数：
   - `userId`: `1002`（普通用户ID）
   - `coachId`: `1001`（教练ID）
3. 应该返回成功

### 4.6 教练查看绑定的用户

1. 先登出：找到 **用户登出** (`POST /api/user/logout`)
2. 用教练账号登录：
   ```json
   {
     "userAccount": "coach001",
     "userPassword": "12345678"
   }
   ```
3. 查看绑定用户：**教练查看绑定的用户列表** (`GET /api/user/coach/assignedUsers`)
4. 应该看到返回包含 `user001` 的列表

### 4.7 普通用户查看教练

1. 登出后用普通用户登录：
   ```json
   {
     "userAccount": "user001",
     "userPassword": "12345678"
   }
   ```
2. 查看教练信息：**普通用户查看自己的教练信息** (`GET /api/user/myCoach`)
3. 应该看到返回 `coach001` 的信息

---

## 第五步：测试权限拦截

### 5.1 用普通用户尝试访问管理接口

确保当前登录的是 `user001`（普通用户），然后：

1. 尝试访问 **获取角色列表** (`GET /api/role/list`)
2. **预期结果**：返回错误
   ```json
   {
     "code": 40101,
     "data": null,
     "message": "权限不足"
   }
   ```

✅ **如果看到这个错误，说明权限拦截正常工作！**

### 5.2 切换回管理员再次访问

1. 登出
2. 用 admin 登录
3. 再次访问 **获取角色列表**
4. 应该成功返回 3 个角色

---

## 第六步：生成前端请求代码

### 6.1 进入前端目录

```powershell
cd E:\Project\OJ_front\yuoj-fron
```

### 6.2 确保后端服务正在运行

访问 http://localhost:8121/api/v2/api-docs 确认能看到 JSON 数据

### 6.3 安装 OpenAPI 生成器（如果还没安装）

```powershell
npm install openapi-typescript-codegen --save-dev
```

### 6.4 生成代码

```powershell
npx openapi-typescript-codegen --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios
```

**或者使用 --yes 跳过确认：**
```powershell
npx --yes openapi-typescript-codegen --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios
```

### 6.5 验证生成结果

检查以下文件是否生成：

```powershell
dir generated\services\RoleControllerService.ts
dir generated\services\PermissionControllerService.ts
dir generated\services\RbacControllerService.ts
dir generated\models\Role.ts
dir generated\models\Permission.ts
```

如果这些文件都存在，说明生成成功！

---

## ✅ 测试检查清单

后端测试：
- [ ] SQL 脚本执行成功
- [ ] 数据库表创建完成（roles, permissions, user_roles, role_permissions）
- [ ] 初始化数据插入成功（3个角色，21个权限）
- [ ] 后端服务启动成功
- [ ] Knife4j 文档可以访问
- [ ] 管理员登录成功
- [ ] 获取当前用户权限返回 21 个权限
- [ ] 查看角色列表返回 3 个角色
- [ ] 查看权限列表返回 21 个权限
- [ ] 创建教练用户成功
- [ ] 为教练分配角色成功
- [ ] 创建普通用户成功
- [ ] 绑定用户到教练成功
- [ ] 教练能查看绑定的用户
- [ ] 普通用户能查看自己的教练
- [ ] 普通用户访问管理接口被拒绝（权限不足）

前端代码生成：
- [ ] openapi-typescript-codegen 安装成功
- [ ] 前端代码生成成功
- [ ] RoleControllerService.ts 文件存在
- [ ] PermissionControllerService.ts 文件存在
- [ ] RbacControllerService.ts 文件存在
- [ ] Role.ts 模型文件存在
- [ ] Permission.ts 模型文件存在

---

## 🎯 成功标志

如果以上所有测试都通过，说明：

✅ **RBAC 权限系统后端已经完全正常工作！**

✅ **前端请求代码已经成功生成！**

下一步可以：
1. 参考 `RBAC_TEST_GUIDE.md` 进行前端对接
2. 开发角色管理页面
3. 开发权限管理页面
4. 开发用户-角色分配页面

---

## ❓ 遇到问题？

### 数据库问题
- **错误**: "Table already exists"
  - **解决**: 表已存在，可以跳过或删除后重建

- **错误**: "Duplicate column name 'coach_id'"
  - **解决**: coach_id 字段已存在，可以跳过 ALTER TABLE 语句

### 服务启动问题
- **错误**: "Address already in use"
  - **解决**: 端口 8121 被占用，修改配置文件或关闭占用进程

- **错误**: "Access denied for user"
  - **解决**: 检查数据库用户名密码是否正确

### 代码生成问题
- **错误**: "Failed to fetch"
  - **解决**: 确保后端服务正在运行，并且可以访问 http://localhost:8121/api/v2/api-docs

- **错误**: "openapi-typescript-codegen not found"
  - **解决**: 运行 `npm install openapi-typescript-codegen --save-dev`

---

**祝测试顺利！** 🎉

有问题随时查看详细文档：
- [RBAC_TEST_GUIDE.md](./RBAC_TEST_GUIDE.md) - 详细测试指南
- [RBAC_IMPLEMENTATION_GUIDE.md](./RBAC_IMPLEMENTATION_GUIDE.md) - 实现指南








