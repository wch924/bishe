# 生成前端 API 接口代码指南

## 📋 生成命令

### 方式1：使用 npm 脚本（推荐）

```bash
npm run generate-api
```

这个命令会：
1. 生成 API 接口代码
2. 自动修复 BASE URL（移除 `/api` 前缀）

### 方式2：手动执行

```bash
# 1. 生成代码
openapi --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios

# 2. 修复 BASE URL
node scripts/fix-openapi-base.js
```

---

## 🔧 为什么需要修复？

### 问题原因

后端配置了 `context-path: /api`：
```yaml
server:
  servlet:
    context-path: /api
```

这意味着：
- OpenAPI 文档中的路径已经包含了 `/api`（如 `/api/user/login`）
- 生成的代码中 `OpenAPI.BASE` 默认也是 `'http://localhost:8121/api'`
- 最终 URL 变成：`http://localhost:8121/api` + `/api/user/login` = `http://localhost:8121/api/api/user/login` ❌

### 解决方案

修复脚本会自动将 `OpenAPI.BASE` 改为 `'http://localhost:8121'`（移除 `/api`），这样：
- 最终 URL：`http://localhost:8121` + `/api/user/login` = `http://localhost:8121/api/user/login` ✅

---

## 📝 修复脚本说明

**文件**：`scripts/fix-openapi-base.js`

**功能**：
- 自动修改 `generated/core/OpenAPI.ts` 中的 `BASE` URL
- 将 `'http://localhost:8121/api'` 改为 `'http://localhost:8121'`

**注意**：
- 脚本会在生成代码后自动运行（如果使用 `npm run generate-api`）
- 也可以单独运行：`node scripts/fix-openapi-base.js`

---

## 🎯 使用步骤

### 1. 确保后端服务运行

```bash
# 后端服务应该在 http://localhost:8121 运行
# OpenAPI 文档地址：http://localhost:8121/api/v2/api-docs
```

### 2. 生成 API 代码

```bash
# 方式1：使用 npm 脚本（推荐）
npm run generate-api

# 方式2：手动执行
openapi --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios
node scripts/fix-openapi-base.js
```

### 3. 验证生成结果

检查 `generated/core/OpenAPI.ts`：
```typescript
export const OpenAPI: OpenAPIConfig = {
    BASE: 'http://localhost:8121',  // ✅ 应该是这个（不包含 /api）
    ...
};
```

检查 `src/main.ts`：
```typescript
OpenAPI.BASE = 'http://localhost:8121';  // ✅ 应该已经设置好了
```

---

## ⚠️ 注意事项

1. **每次重新生成代码后**：
   - 如果使用 `npm run generate-api`，会自动修复
   - 如果手动生成，记得运行修复脚本

2. **BASE URL 配置**：
   - `src/main.ts` 中已经设置了 `OpenAPI.BASE = 'http://localhost:8121'`
   - 这会覆盖生成的默认值，所以即使不修复也能工作
   - 但为了代码一致性，建议还是修复生成的代码

3. **后端 context-path**：
   - 如果修改后端的 `context-path`，需要同步修改：
     - `scripts/fix-openapi-base.js` 中的替换规则
     - `src/main.ts` 中的 `OpenAPI.BASE`

---

## 🧪 验证

生成代码后，检查一个接口的 URL：

**示例**：`UserControllerService.getLoginUserUsingGet()`

- OpenAPI 文档路径：`/api/user/get/login`
- `OpenAPI.BASE`：`http://localhost:8121`
- 最终 URL：`http://localhost:8121/api/user/get/login` ✅

---

## 📚 相关文件

- `package.json` - 包含 `generate-api` 脚本
- `scripts/fix-openapi-base.js` - 自动修复脚本
- `generated/core/OpenAPI.ts` - 生成的 OpenAPI 配置
- `src/main.ts` - 应用入口，覆盖 BASE URL

---

**文档更新时间**：2025-11-18  
**版本**：v1.0.0




