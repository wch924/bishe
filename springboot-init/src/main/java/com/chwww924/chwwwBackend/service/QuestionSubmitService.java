package com.chwww924.chwwwBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chwww924.chwwwBackend.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.chwww924.chwwwBackend.model.entity.QuestionSubmit;
import com.chwww924.chwwwBackend.model.entity.User;

/**
* @author ASUS
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2024-05-05 21:14:14
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    long doQuestionSubmit (QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);
}
