package com.chwww924.chwwwBackend.judge.codesandbox.model;

import lombok.Data;


@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;
    /**
     * 消耗内存（KB）
     */
    private long memory;
    /**
     * 消耗时间（KB）
     */
    private long time;


}
