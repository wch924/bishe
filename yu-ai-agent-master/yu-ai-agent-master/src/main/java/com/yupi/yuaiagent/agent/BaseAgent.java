package com.yupi.yuaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.yupi.yuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    // 流式输出时持有的 SseEmitter（仅在 runStream 期间有效，供子类 think/act 使用）
    private transient SseEmitter activeSseEmitter = null;

    // ─────────────────────────────────────────────────────────────────────────
    // SSE 结构化 JSON 工具方法（供子类 think / act 调用）
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 发送结构化进度事件
     *
     * @param step       步骤描述文本
     * @param status     "running" 或 "completed"
     * @param isThinking 是否为思考阶段（true 则前端显示旋转 spinner）
     */
    protected void sendProgress(String step, String status, boolean isThinking) {
        if (activeSseEmitter == null) return;
        try {
            String json = String.format(
                    "{\"type\":\"progress\",\"step\":\"%s\",\"status\":\"%s\",\"isThinking\":%b}",
                    escapeJson(step), status, isThinking
            );
            activeSseEmitter.send(json);
        } catch (IOException e) {
            log.warn("Failed to send progress SSE event: {}", e.getMessage());
        }
    }

    /**
     * 发送最终文本内容块（前端将其追加到对话气泡的文本区）
     */
    protected void sendContent(String data) {
        if (activeSseEmitter == null) return;
        try {
            String json = String.format(
                    "{\"type\":\"content\",\"data\":\"%s\"}",
                    escapeJson(data)
            );
            activeSseEmitter.send(json);
        } catch (IOException e) {
            log.warn("Failed to send content SSE event: {}", e.getMessage());
        }
    }

    /**
     * 发送错误事件（前端显示红色提示）
     */
    protected void sendError(String message) {
        if (activeSseEmitter == null) return;
        try {
            String json = String.format(
                    "{\"type\":\"error\",\"message\":\"%s\"}",
                    escapeJson(message)
            );
            activeSseEmitter.send(json);
        } catch (IOException e) {
            log.warn("Failed to send error SSE event: {}", e.getMessage());
        }
    }

    /**
     * 发送完成事件并正常关闭连接
     */
    protected void sendDone(SseEmitter emitter) {
        try {
            emitter.send("{\"type\":\"done\"}");
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 简单 JSON 字符串转义（避免引入额外依赖）
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 运行代理（同步）
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        this.state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                String stepResult = step();
                results.add("Step " + stepNumber + ": " + stepResult);
            }
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            this.cleanup();
        }
    }

    /**
     * 运行代理（SSE 流式输出）
     * <p>
     * 在整个执行期间将 SseEmitter 存储到 activeSseEmitter，
     * 以便子类的 think() / act() 可通过 sendProgress / sendContent 等方法推送结构化 JSON 事件。
     *
     * @param userPrompt 用户提示词
     * @return SseEmitter
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时
        // 存储到实例字段，供子类使用
        this.activeSseEmitter = sseEmitter;

        CompletableFuture.runAsync(() -> {
            // 1、基础校验
            try {
                if (this.state != AgentState.IDLE) {
                    sendError("无法从状态运行代理：" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sendError("不能使用空提示词运行代理");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
                return;
            }

            // 2、执行
            this.state = AgentState.RUNNING;
            messageList.add(new UserMessage(userPrompt));
            try {
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    // step() 内部的 think/act 会自动推送 progress 事件 / content
                    String stepResult = step();
                    // 不再盲目将 stepResult 推送到前台作为 content
                    log.debug("Step {} result: {}", stepNumber, stepResult);
                }
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    sendContent("执行结束：已达到最大步骤（" + maxSteps + "）");
                }
                // 正常完成
                sendDone(sseEmitter);
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent", e);
                sendError("执行错误：" + e.getMessage());
                try {
                    sseEmitter.complete();
                } catch (Exception ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                this.activeSseEmitter = null;
                this.cleanup();
            }
        });

        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.activeSseEmitter = null;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.activeSseEmitter = null;
            this.cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    /**
     * 定义单个步骤（子类实现）
     */
    public abstract String step();

    /**
     * 清理资源（子类可重写）
     */
    protected void cleanup() {
        // no-op by default
    }
}
