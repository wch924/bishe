package com.chwww924.chwwwBackend.judge.strategy;

import com.chwww924.chwwwBackend.model.dto.question.JudgeCase;
import com.chwww924.chwwwBackend.judge.codesandbox.model.JudgeInfo;
import com.chwww924.chwwwBackend.model.entity.Question;
import com.chwww924.chwwwBackend.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;
@Data
public class JudgeContext {
    private List<String> inputList;
    private List<String> outputList;
    private JudgeInfo judgeInfo;
    private List<JudgeCase> judgeCaseList;
    private Question question;
    private QuestionSubmit questionSubmit;
}
