# RBAC 系统实现问题总结

本文档记录了在实现 RBAC（Role-Based Access Control）权限系统过程中遇到的所有问题及解决方案。

---

## 📋 目录

1. [前端编译错误](#1-前端编译错误)
2. [后端数据库字段映射问题](#2-后端数据库字段映射问题)
3. [前端 API 方法名问题](#3-前端-api-方法名问题)
4. [总结与经验](#4-总结与经验)

---

## 1. 前端编译错误

### 问题 1.1: 模块路径错误

**错误信息**:
```
Cannot find module '@/generated'
```

**原因分析**:
- `@` 别名指向 `src` 目录
- `generated` 文件夹在项目根目录
- 路径 `@/generated` 实际指向 `src/generated`，但该目录不存在

**解决方案**:
修改导入路径，使用相对路径：
- `src/main.ts`: `'@/generated'` → `'../generated'`
- `src/views/admin/RbacTestView.vue`: `'@/generated'` → `'../../../generated'`

**修改的文件**:
- `yuoj-fron/src/main.ts`
- `yuoj-fron/src/views/admin/RbacTestView.vue`

---

### 问题 1.2: 环境变量类型错误

**错误信息**:
```
TS2339: Property 'env' does not exist on type 'ImportMeta'
```

**原因分析**:
TypeScript 不识别 Vite 的 `import.meta.env` 类型定义

**解决方案**:
1. **方案一（临时）**: 移除环境变量使用，硬编码 API 地址
   ```typescript
   // 修改前
   OpenAPI.BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8121/api';
   
   // 修改后
   OpenAPI.BASE = 'http://localhost:8121/api';
   ```

2. **方案二（推荐）**: 创建类型声明文件
   ```typescript
   // yuoj-fron/src/env.d.ts
   interface ImportMetaEnv {
     readonly VITE_API_BASE_URL: string
   }
   
   interface ImportMeta {
     readonly env: ImportMetaEnv
   }
   ```

**修改的文件**:
- `yuoj-fron/src/main.ts`
- `yuoj-fron/src/env.d.ts` (新增)

---

### 问题 1.3: WebSocket 类型错误

**错误信息 A**:
```
TS2769: No overload matches this call.
Argument of type '(error: any) => void' is not assignable to parameter of type '() => void'
```

**错误信息 B**:
```
TS2322: Type 'string | string[] | undefined' is not assignable to type 'string | string[]'
```

**原因分析**:
1. `error` 事件回调函数签名不匹配
2. `protocols` 属性可能为 `undefined`，与 `Required<WebSocketOptions>` 类型冲突

**解决方案**:

**A. 修复 error 回调** (`websocket-examples.ts`):
```typescript
// 修改前
on('error', (error) => {
  console.error('连接错误:', error);
});

// 修改后
on('error', () => {
  console.error('连接错误');
});
```

**B. 修复 protocols 类型** (`websocket.ts`):
```typescript
// 修改前
this.config = {
    ...DEFAULT_CONFIG,
    ...options
} as Required<WebSocketOptions>;

// 修改后
this.config = {
    ...DEFAULT_CONFIG,
    ...options,
    protocols: options.protocols || []
} as Required<WebSocketOptions>;
```

**修改的文件**:
- `yuoj-fron/src/utils/websocket-examples.ts`
- `yuoj-fron/src/utils/websocket.ts`

---

## 2. 后端数据库字段映射问题

### 问题 2.1: coachId 字段找不到

**错误信息**:
```
java.sql.SQLSyntaxErrorException: Unknown column 'coachId' in 'field list'
```

**SQL 语句**:
```sql
SELECT id,userAccount,userPassword,...,coachId,... FROM user WHERE ...
```

**原因分析**:
1. 实体类字段: `coachId` (驼峰命名)
2. 数据库字段: `coach_id` (下划线命名)
3. MyBatis Plus 配置: `map-underscore-to-camel-case: false`
4. 因此 MyBatis Plus 不会自动转换，导致字段名不匹配

**错误的解决尝试**:
最初尝试将 `map-underscore-to-camel-case` 改为 `true`，但这导致了新问题：所有其他字段（如 `userAccount`）也会被转换为下划线格式（`user_account`），而数据库中这些字段本身就是驼峰格式。

**正确解决方案**:
保持 `map-underscore-to-camel-case: false`，使用 `@TableField` 注解单独映射：

```java
@TableField("coach_id")
private Long coachId;
```

**修改的文件**:
- `springboot-init/src/main/java/com/chwww924/chwwwBackend/model/entity/User.java`
- `springboot-init/src/main/resources/application.yml` (保持 false)

---

### 问题 2.2: Role 实体字段找不到

**错误信息**:
```
java.sql.SQLSyntaxErrorException: Unknown column 'roleName' in 'field list'
```

**SQL 语句**:
```sql
SELECT id,roleName,roleKey,description,isSystem,... FROM roles WHERE ...
```

**数据库实际字段名**:
```
role_name, role_key, is_system
```

**原因分析**:
与 `coachId` 问题相同，实体类使用驼峰命名，数据库使用下划线命名

**解决方案**:
为 `Role` 实体类的所有下划线字段添加 `@TableField` 注解：

```java
@TableField("role_name")
private String roleName;

@TableField("role_key")
private String roleKey;

@TableField("is_system")
private Integer isSystem;
```

**修改的文件**:
- `springboot-init/src/main/java/com/chwww924/chwwwBackend/model/entity/Role.java`

---

### 问题 2.3: Permission 实体字段找不到

**错误信息**:
```
java.sql.SQLSyntaxErrorException: Unknown column 'permissionName' in 'field list'
```

**数据库实际字段名**:
```
permission_name, permission_key, resource_type
```

**解决方案**:
为 `Permission` 实体类添加 `@TableField` 注解：

```java
@TableField("permission_name")
private String permissionName;

@TableField("permission_key")
private String permissionKey;

@TableField("resource_type")
private String resourceType;
```

**修改的文件**:
- `springboot-init/src/main/java/com/chwww924/chwwwBackend/model/entity/Permission.java`

---

## 3. 前端 API 方法名问题

### 问题 3.1: API 方法不存在

**错误信息**:
```
TypeError: RoleControllerService.listRoles is not a function
TypeError: PermissionControllerService.listPermissions is not a function
```

**原因分析**:
OpenAPI 代码生成器生成的方法名带有 HTTP 方法后缀：
- 生成的方法名: `listRolesUsingGet()`, `listPermissionsUsingGet()`
- 代码中调用: `listRoles()`, `listPermissions()`

**解决方案**:
修改前端代码，使用正确的方法名：

```typescript
// 修改前
const res = await RoleControllerService.listRoles();
const res = await PermissionControllerService.listPermissions();

// 修改后
const res = await RoleControllerService.listRolesUsingGet();
const res = await PermissionControllerService.listPermissionsUsingGet();
```

**修改的文件**:
- `yuoj-fron/src/views/admin/RbacTestView.vue`

---

### 问题 3.2: API URL 前缀重复

**问题描述**:
生成的服务代码中 URL 带有 `/api` 前缀，而 `OpenAPI.BASE` 配置中也包含 `/api`，导致实际请求路径为 `/api/api/...`

**解决方案**:
批量删除所有生成的服务文件中的 `/api` 前缀：

```powershell
cd E:\Project\OJ_front\yuoj-fron\generated\services
Get-ChildItem -Filter "*.ts" | ForEach-Object { 
    (Get-Content $_.FullName -Raw) -replace "url: '/api/", "url: '/" | 
    Set-Content $_.FullName -NoNewline 
}
```

**修改前**:
```typescript
url: '/api/rbac/current/permissions'
```

**修改后**:
```typescript
url: '/rbac/current/permissions'
```

**修改的文件**:
- `yuoj-fron/generated/services/*.ts` (所有服务文件)

---

## 4. 总结与经验

### 4.1 数据库设计规范问题

**核心问题**:
项目数据库表存在命名规范不一致：
- 旧表（`user`）: 使用驼峰命名 (`userAccount`, `userPassword`)
- 新表（`roles`, `permissions`）: 使用下划线命名 (`role_name`, `permission_key`)

**影响**:
- 无法统一使用 MyBatis Plus 的驼峰转换功能
- 必须手动为每个下划线字段添加 `@TableField` 注解
- 增加了维护成本

**建议**:
1. **项目初期就确定命名规范**，要么全用驼峰，要么全用下划线
2. **推荐使用下划线命名**（数据库标准）+ MyBatis Plus 自动驼峰转换
3. 如果要改造旧项目，考虑数据库迁移统一命名规范

---

### 4.2 前端 API 代码生成问题

**经验总结**:

1. **OpenAPI 代码生成配置**:
   - 生成的方法名会包含 HTTP 方法后缀 (`UsingGet`, `UsingPost`)
   - 需要在使用前检查实际生成的方法签名

2. **URL 路径配置**:
   - `OpenAPI.BASE` 应该只配置基础域名和上下文路径
   - 生成的服务代码中的 URL 应该是相对路径（不含 base 部分）
   - 需要手动处理或通过生成配置避免路径重复

3. **模块路径问题**:
   - 注意 TypeScript 路径别名的作用范围
   - 生成的代码放在 `src` 外时，避免使用路径别名

---

### 4.3 类型安全检查

**收获**:
1. TypeScript 的严格类型检查帮助及早发现问题
2. 为第三方库或自定义配置添加类型声明文件很重要
3. WebSocket 等复杂类型需要仔细处理可选属性

---

### 4.4 错误排查流程

**有效的排查步骤**:

1. **查看完整错误堆栈**
   - 找到错误的根本原因（Caused by）
   - 定位到具体的代码行

2. **检查 SQL 日志**
   - MyBatis Plus 会打印实际执行的 SQL
   - 对比 SQL 字段名与数据库实际字段名

3. **验证数据库结构**
   ```sql
   DESC table_name;
   ```

4. **逐步验证假设**
   - 不要一次改动太多
   - 每次改动后重启服务验证结果

---

## 5. 完整修改清单

### 前端文件修改

| 文件路径 | 修改内容 | 问题编号 |
|---------|---------|---------|
| `src/main.ts` | 修改 generated 导入路径、删除环境变量使用 | 1.1, 1.2 |
| `src/env.d.ts` | **新增** 环境变量类型声明 | 1.2 |
| `src/utils/websocket.ts` | 修复 protocols 类型问题 | 1.3 |
| `src/utils/websocket-examples.ts` | 修复 error 回调参数 | 1.3 |
| `src/views/admin/RbacTestView.vue` | 修改导入路径、修正方法名 | 1.1, 3.1 |
| `src/store/rbac.ts` | 修正 API 方法名 | 3.1 |
| `generated/services/*.ts` | 批量删除 `/api` 前缀 | 3.2 |

### 后端文件修改

| 文件路径 | 修改内容 | 问题编号 |
|---------|---------|---------|
| `model/entity/User.java` | 添加 `@TableField("coach_id")` | 2.1 |
| `model/entity/Role.java` | 添加多个 `@TableField` 注解 | 2.2 |
| `model/entity/Permission.java` | 添加多个 `@TableField` 注解 | 2.3 |
| `resources/application.yml` | 保持 `map-underscore-to-camel-case: false` | 2.1 |

---

## 6. 预防措施

### 6.1 前端开发

✅ **使用 TypeScript 严格模式**
```json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true
  }
}
```

✅ **配置路径别名时考虑代码生成目录**
```javascript
// vue.config.js 或 vite.config.js
resolve: {
  alias: {
    '@': path.resolve(__dirname, 'src'),
    '@generated': path.resolve(__dirname, 'generated')
  }
}
```

✅ **API 代码生成后立即检查**
- 查看生成的方法签名
- 验证 URL 路径是否正确
- 测试类型定义是否完整

---

### 6.2 后端开发

✅ **统一数据库命名规范**
- 推荐：数据库下划线 + MyBatis 驼峰转换
- 或者：全部使用驼峰（不推荐，不符合 SQL 规范）

✅ **实体类模板**
```java
@TableName(value = "table_name")
@Data
public class Entity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("db_field_name")  // 明确映射
    private String javaFieldName;
    
    // 日期字段
    private Date createTime;
    private Date updateTime;
    
    // 逻辑删除
    @TableLogic
    private Integer isDelete;
}
```

✅ **配置 MyBatis Plus 日志**
```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```
可以看到实际执行的 SQL，便于调试

---

## 7. 相关文档

- [QUICK_START.md](./QUICK_START.md) - 快速启动指南
- [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md) - 实现总结
- [FIXES_APPLIED.md](./FIXES_APPLIED.md) - 前端编译错误修复

---

## 8. 问题记录时间线

| 时间 | 问题 | 状态 |
|------|------|------|
| 2025-11-17 20:30 | 前端编译错误（模块路径、环境变量、WebSocket） | ✅ 已解决 |
| 2025-11-17 21:15 | 后端 coachId 字段映射错误 | ✅ 已解决 |
| 2025-11-17 21:22 | 后端 user_account 字段映射错误（错误尝试） | ⚠️ 回滚 |
| 2025-11-17 21:30 | 前端 API 方法名错误 | ✅ 已解决 |
| 2025-11-17 21:48 | 后端 Role/Permission 字段映射错误 | ✅ 已解决 |

---

**文档最后更新**: 2025-11-17 22:00  
**状态**: ✅ 所有问题已解决，系统可正常运行





