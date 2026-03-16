package com.chwww924.chwwwBackend.judge.codesandbox.model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ExecuteCodeResponse {
    private List<String> outputList;
    private String message;
    private Integer status;
    private JudgeInfo judgeInfo;
}
