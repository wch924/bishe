package com.chwww924.chwwwBackend.judge.strategy;

import com.chwww924.chwwwBackend.judge.codesandbox.model.JudgeInfo;

public interface JudgeStrategy {
    JudgeInfo doJudge(JudgeContext judgeContext);
}
