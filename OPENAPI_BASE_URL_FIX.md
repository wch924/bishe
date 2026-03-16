# OpenAPI Base URL 修复说明

## 🐛 问题描述

使用 `openapi --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios` 生成前端接口代码后，生成的 URL 中会多一个 `/api`，导致路径变成 `/api/api/xxx`。

## 🔍 问题原因

### 后端配置

在 `springboot-init/src/main/resources/application.yml` 中：
```yaml
server:
  servlet:
    context-path: /api
```

这意味着：
- 后端所有接口的实际路径都是 `/api/xxx`
- OpenAPI 文档中的路径也包含了 `/api` 前缀（如 `/api/user/login`）

### 生成的代码

生成的 `generated/core/OpenAPI.ts` 中：
```typescript
export const OpenAPI: OpenAPIConfig = {
    BASE: 'http://localhost:8121/api',  // ❌ 这里又加了一次 /api
    ...
};
```

### 问题分析

1. OpenAPI 文档中的路径：`/api/user/login`
2. `OpenAPI.BASE`：`http://localhost:8121/api`
3. 最终 URL：`http://localhost:8121/api` + `/api/user/login` = `http://localhost:8121/api/api/user/login` ❌

---

## ✅ 解决方案

### 方案1：修改生成的代码（推荐）

**修改 `generated/core/OpenAPI.ts`**：
```typescript
export const OpenAPI: OpenAPIConfig = {
    BASE: 'http://localhost:8121',  // ✅ 移除 /api，因为 OpenAPI 文档中的路径已经包含了
    ...
};
```

**修改 `src/main.ts`**：
```typescript
// 配置 API 基础地址
// 注意：后端已配置 context-path: /api，所以这里不需要再加 /api
OpenAPI.BASE = 'http://localhost:8121';
```

### 方案2：使用 openapi-typescript-codegen 的 base 参数

在生成代码时指定 base URL：
```bash
openapi --input http://localhost:8121/api/v2/api-docs --output ./generated --client axios --base http://localhost:8121
```

但是，`openapi-typescript-codegen` 可能不支持 `--base` 参数，所以还是需要手动修改生成的代码。

---

## 🔧 修复步骤

### 1. 修改生成的代码

**文件1：`generated/core/OpenAPI.ts`**
```typescript
BASE: 'http://localhost:8121',  // 移除 /api
```

**文件2：`src/main.ts`**
```typescript
OpenAPI.BASE = 'http://localhost:8121';  // 移除 /api
```

### 2. 验证修复

生成的 URL 应该是：
- `http://localhost:8121` + `/api/user/login` = `http://localhost:8121/api/user/login` ✅

---

## 📝 注意事项

1. **每次重新生成代码后**：
   - 需要手动修改 `generated/core/OpenAPI.ts` 中的 `BASE` 值
   - 或者在 `src/main.ts` 中覆盖 `OpenAPI.BASE`（推荐）

2. **推荐做法**：
   - 在 `src/main.ts` 中设置 `OpenAPI.BASE = 'http://localhost:8121'`
   - 这样即使重新生成代码，也不会影响

3. **后端 context-path**：
   - 如果修改后端的 `context-path`，需要同步修改前端的 `OpenAPI.BASE`

---

## 🎯 最终配置

### 后端配置（不变）
```yaml
server:
  servlet:
    context-path: /api
```

### 前端配置（已修复）
```typescript
// src/main.ts
OpenAPI.BASE = 'http://localhost:8121';  // 不包含 /api
```

### 生成的代码（需要修改）
```typescript
// generated/core/OpenAPI.ts
BASE: 'http://localhost:8121',  // 不包含 /api
```

---

## 🧪 验证

生成的 URL 示例：
- OpenAPI 文档路径：`/api/user/login`
- `OpenAPI.BASE`：`http://localhost:8121`
- 最终 URL：`http://localhost:8121/api/user/login` ✅

---

**文档更新时间**：2025-11-18  
**版本**：v1.0.0




