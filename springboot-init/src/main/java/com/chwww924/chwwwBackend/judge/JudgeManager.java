package com.chwww924.chwwwBackend.judge;

import com.chwww924.chwwwBackend.judge.strategy.DefaultJudgeStrategy;
import com.chwww924.chwwwBackend.judge.strategy.JavaJudgeStrategy;
import com.chwww924.chwwwBackend.judge.strategy.JudgeContext;
import com.chwww924.chwwwBackend.judge.strategy.JudgeStrategy;
import com.chwww924.chwwwBackend.judge.codesandbox.model.JudgeInfo;
import com.chwww924.chwwwBackend.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

@Service
public class JudgeManager {
    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if (language == "java"){
            judgeStrategy = new JavaJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
