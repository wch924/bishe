# AI 部分集成与开发指南

本文档旨在帮助开发人员快速了解本项目中 AI 模块的功能、实现原理、关键配置以及完整的交互链路。阅读本文档后，技术人员可以直接上手进行 AI 功能的维护与二次开发。

---

## 1. 概述：AI 在做什么？

本项目集成了强大的 AI 能力，主要提供了两类 AI 应用：
1. **AI 运动健康大师 (`SportApp`)**：专注于设定领域（运动健康/情感咨询）的 AI 助手。支持上下文记忆、RAG（检索增强生成）、外部工具调用（Tool Calling）以及 MCP（Model Context Protocol）服务集成。
2. **AI 超级智能体 (`YuManus`)**：通用型超级智能体，拥有自主规划能力，能根据用户需求自动分析并调用多种复杂工具（如文件操作、网页搜索、PDF生成等）来解决综合性问题。

前端通过 **SSE (Server-Sent Events)** 技术与后端建立长连接，实现 AI 回复的打字机（流式）输出效果。

---

## 2. 架构与交互流转

用户的请求从前端发起，通过 SSE 建立数据流通信直达后端，后端再向大模型 (LLM) 获取流式数据并推回给前端。

```text
+----------------+       SSE (EventSource)      +--------------------+      HTTP / WebSocket     +-------------------+
|                | ---------------------------> |                    | ------------------------> |                   |
| yuoj-fron (前端)|                           | yu-ai-agent (后端)  |                           | 大模型 (Qwen/Ollama)|
|                | <--------------------------- |                    | <------------------------ |                   |
+----------------+    流式数据块 (data: chunk)   +--------------------+       流式 token 回复      +-------------------+
```

---

## 3. 后端实现指南 (yu-ai-agent-master)

后端基于 **Spring AI (Alibaba)** 框架构建，位于 `com.yupi.yuaiagent` 包下。

### 3.1 核心配置文件 (`src/main/resources/application.yml`)
在进行任何开发前，需要先配置大模型的 API Key 或本地模型地址：
```yaml
spring:
  ai:
    dashscope: # 阿里云百炼大模型配置
      api-key: sk-xxxxxxxxxxxx # 替换为你的大模型 API Key
      chat:
        options:
          model: qwen-plus # 默认使用的模型
    ollama: # 本地部署的大模型配置 (按需)
      base-url: http://localhost:11434
      chat:
        model: gemma3:1b
```
> **提示**：目前项目默认使用 `dashscope` (通义千问)，若要看更多 Spring AI 内部调用细节，配置文件中可以开启 `logging.level.org.springframework.ai: DEBUG`。

### 3.2 两种 AI 核心组件实现原理
后端通过构建不同配置的 `ChatClient` 来实现不同维度的 AI 能力。

#### A. 领域专家应用 (`SportApp.java`)
**位置**：`app/SportApp.java`
- **初始化模式**：通过 `ChatClient.builder()` 构建，并注入了 System Prompt，限定其为领域专家。
- **对话记忆**：默认使用了基于内存的 `MessageWindowChatMemory`（最多保留 20 条消息上下文），通过 `MessageChatMemoryAdvisor` 增强进去。
- **流式输出**：`doChatByStream` 方法直接调用了 `.stream().content()`，返回 `Flux<String>`，利用 Spring WebFlux 的响应式特性对接 SSE。
- **进阶能力**：内置了与 PgVector 或在线知识库对接的 RAG (检索增强) 功能代码 (`doChatWithRag`)，以及工具调用 (`doChatWithTools`)。

#### B. 超级智能体 (`YuManus.java`)
**位置**：`agent/YuManus.java`
- **初始化模式**：继承自通用的 `ToolCallAgent`，拥有更高级的 "Thinking" 规划提示词 (System / Next Step Prompt)。
- **流式输出**：并不直接返回 Flux，而是通过在内部实例化 `SseEmitter` (`new SseEmitter(180000L)`) 进行更加传统且可控的生命周期管理（超时、错误终止等），由 Agent 自己手动 `.send(chunk)` 发送实时文本。

### 3.3 接口暴露 (`AiController.java`)
**位置**：`controller/AiController.java`
- 前端对接的最核心类。包含了不同的 SSE 接口示例：
  - `/ai/love_app/chat/sse`: 响应式流 `Flux<String>`（`produces = MediaType.TEXT_EVENT_STREAM_VALUE`）。
  - `/ai/love_app/chat/sse_emitter`: `SseEmitter` 传统长连接模式。
  - `/ai/manus/chat`: Manus 智能体的专用接口。

### 3.4 扩展工具箱 (`tools/`)
**位置**：`tools/` 目录下（如 `WebSearchTool`, `FileOperationTool` 等）
Spring AI 允许模型自主调用这些工具。如果要新增工具，只需：
1. 实现一个带有 `@Description` 的普通方法或者实现相关接口。
2. 确保在传入 `YuManus` 时，在构造器的 `ToolCallback[] allTools` 里被包含到。

---

## 4. 前端实现指南 (yuoj-fron)

前端部分主要负责与接口通信并丝滑地展现 AI 打字效果。

### 4.1 SSE 接口对接 (`src/api/index.ts`)
这里不使用常见的 `axios.post`，而是使用原生的 `EventSource` (或封装的 SSE 管理器) 发起连接。
```typescript
export const doChatWithLoveAppSse = (message: string, chatId: string) => {
  const url = `/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${chatId}`;
  return new EventSource(API_BASE_URL + url);
}
```

### 4.2 核心聊天组件 (`ChatRoom.vue`)
**位置**：`src/components/ChatRoom.vue`
这个组件封装了**完整的流式消息发送与渲染逻辑**，是前端 AI 的核心枢纽。
- 发送消息时，创建一个 `EventSource` 实例（通过 `props.chatApi` 注入上述 API 函数）。
- 监听 `eventSource.onmessage`，收到新 `event.data` 数据块 (chunk) 时，追加合并到当前对话最后一条 `AI Message` 的内容中。
- 当接收到 `[DONE]` 标识，或是连接结束时，触发 `eventSource.close()`。
- 组件内部负责每次消息更新后的**自动滚动到底部**。

### 4.3 业务页面视图 (`LoveApp.vue` & `ManusApp.vue`)
**位置**：`src/views/ai/`
这两个文件都是壳子，非常简单，只需要：
1. 引入并使用 `<ChatRoom>`。
2. 将对应后端的 API 方法（如 `doChatWithLoveAppSse` 或 `doChatWithManus`）传入给 `ChatRoom`。
3. 指定界面 Title 及是否需要携带 `chatId`（用于上下文隔离）。

---

## 5. 快速上手 & 开发配置演练

### 场景一：更换大模型 API Key / 更换大模型
如果你想换成自己的 Key，或者将 `qwen-plus` 升级到 `qwen-max`：
1. 打开后端 `yu-ai-agent-master/src/main/resources/application.yml`。
2. 找到 `spring.ai.dashscope.api-key` 改为你的阿里云 SDK Key。
3. 找到 `spring.ai.dashscope.chat.options.model` 修改模型代号即可。

### 场景二：如何修改/优化大模型的基础人设 (Prompt)？
- **对于普通的助手**：修改 `com.yupi.yuaiagent.app.SportApp` 中的 `SYSTEM_PROMPT` 常量。
- **对于超级智能体**：修改 `com.yupi.yuaiagent.agent.YuManus` 构造函数里的 `SYSTEM_PROMPT` 字符串。

### 场景三：在前端新增一个专属 AI 频道？
1. 在后端的 `AiController.java` 中新增一个 GetMapping，比如 `/ai/my_app/chat`。
2. 在前端 `src/api/index.ts` 中新增 API 方法 `doChatWithMyApp(message) { return new EventSource(...) }`。
3. 在前端 `src/views/ai/` 复制一份 `LoveApp.vue`，重命名为 `MyApp.vue`。
4. 在其中把 `chatApi` 换成你刚才写的 `doChatWithMyApp`，重设 `title="专属频道"`。
5. 在 `router` 中注册新的路由即可。

### 场景四：如果我想让 AI 能够查询公司内部数据库？
无需自己解析大模型返回：
1. 在后端 `tools/` 目录下编写一个新的 Spring Bean（如 `DatabaseQueryTool`），提供带有详尽 `@Description` 注解的 `Function` 被 Spring AI 识别。
2. 在 `SportApp` 或 `YuManus` 中引入该 Tool 并注册至 `ChatClient` 初始化时的 `toolCallbacks`。
3. 用户提问时，基座大模型（如果是支持 Function Calling 的模型）会自动判断并在对话中途隐式触发这个工具，获取数据库结果后再总结返回给前端。
