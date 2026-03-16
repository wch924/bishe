package com.chwww924.chwwwBackend.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.chwww924.chwwwBackend.model.dto.question.JudgeCase;
import com.chwww924.chwwwBackend.model.dto.question.JudgeConfig;
import com.chwww924.chwwwBackend.judge.codesandbox.model.JudgeInfo;
import com.chwww924.chwwwBackend.model.entity.Question;
import com.chwww924.chwwwBackend.model.enums.JudgeInfoMessage;

import java.util.List;

public class DefaultJudgeStrategy implements JudgeStrategy{
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judeCaseList = judgeContext.getJudgeCaseList();
        long memory = judgeInfo.getMemory();
        long time = judgeInfo.getTime();
        JudgeInfoMessage judgeInfoMessage = JudgeInfoMessage.ACCEPTED;
        JudgeInfo judgeInfoRes = new JudgeInfo();
        judgeInfoRes.setMessage(judgeInfoMessage.getValue());
        judgeInfoRes.setMemory(memory);
        judgeInfoRes.setTime(time);
        if (outputList.size()!=inputList.size()){
            judgeInfoMessage = JudgeInfoMessage.WRONG_ANSWER;
            return judgeInfoRes;
        }
        for (int i = 0;i < judeCaseList.size();i++){
            if (!outputList.get(i).equals(judeCaseList.get(i).getOutput())){
                judgeInfoMessage = JudgeInfoMessage.WRONG_ANSWER;
                return judgeInfoRes;
            }
        }

        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        long needMemory = judgeConfig.getMemoryLimit();
        long needTime = judgeConfig.getTimeLimit();
        if (memory > needMemory){
            judgeInfoMessage = JudgeInfoMessage.Memory_Limit_Exceeded;
            return judgeInfoRes;
        }
        if (time > needTime){
            judgeInfoMessage = JudgeInfoMessage.TIME_LIMIT_ERROR;
            return judgeInfoRes;
        }

        return judgeInfoRes;
    }
}
