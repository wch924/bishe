package com.chwww924.chwwwBackend.controller;

import com.chwww924.chwwwBackend.common.BaseResponse;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.common.ResultUtils;
import com.chwww924.chwwwBackend.service.QuestionSubmitService;

import com.chwww924.chwwwBackend.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子收藏接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {
    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 提交 
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum 本次提交题目数量
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                         HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final User loginUser = userService.getLoginUser(request);

        long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }

}
