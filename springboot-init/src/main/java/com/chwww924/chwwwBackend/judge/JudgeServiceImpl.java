package com.chwww924.chwwwBackend.judge;

import cn.hutool.json.JSONUtil;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.judge.codesandbox.CodeSandbox;
import com.chwww924.chwwwBackend.judge.codesandbox.CodeSandboxFactory;
import com.chwww924.chwwwBackend.judge.codesandbox.CodeSandboxProxy;
import com.chwww924.chwwwBackend.judge.codesandbox.model.ExcuteCodeRequest;
import com.chwww924.chwwwBackend.judge.codesandbox.model.ExecuteCodeResponse;
import com.chwww924.chwwwBackend.judge.strategy.JudgeContext;
import com.chwww924.chwwwBackend.model.dto.question.JudgeCase;
import com.chwww924.chwwwBackend.judge.codesandbox.model.JudgeInfo;
import com.chwww924.chwwwBackend.model.entity.Question;
import com.chwww924.chwwwBackend.model.entity.QuestionSubmit;
import com.chwww924.chwwwBackend.model.enums.QuestionSubmitStatusEnum;
import com.chwww924.chwwwBackend.service.QuestionService;
import com.chwww924.chwwwBackend.service.QuestionSubmitService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService{
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private QuestionService questionService;
    @Resource
    private JudgeManager judgeManager;
    @Value("${codesandbox.type:example}")
    private String type;
    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交信息不存在");
        }
        Long id = questionSubmit.getId();
        Question question = questionService.getById(questionSubmit.getQuestionId());
        if (question == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        }
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"题目正在判题中");
        }
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新提交状态失败");
        }

        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        List<String> inputList = judeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExcuteCodeRequest executeCodeRequest = ExcuteCodeRequest.builder()
                .code(code)
                .inputList(inputList)
                .language(language)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.excuteCode(executeCodeRequest);
        List<String>outputList = executeCodeResponse.getOutputList();
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setJudgeCaseList(judeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);


        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudegeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);
        return questionSubmitResult;
    }
}
