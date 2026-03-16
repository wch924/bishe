package com.chwww924.chwwwBackend.model.dto.question;

import lombok.Data;

@Data
public class JudgeConfig {
    /**
     * 时间限制（ms）
     */
    private long timeLimit;
    /**
     * 内存限制（KB）
     */
    private long memoryLimit;
    /**
     * 栈堆限制（KB）
     */
    private long stackLimit;
}
