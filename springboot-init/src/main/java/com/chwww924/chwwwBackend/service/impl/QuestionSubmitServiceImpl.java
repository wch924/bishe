package com.chwww924.chwwwBackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.chwww924.chwwwBackend.model.enums.QuestionSubmitStatusEnum;
import com.chwww924.chwwwBackend.service.QuestionService;
import com.chwww924.chwwwBackend.service.QuestionSubmitService;
import com.chwww924.chwwwBackend.mapper.QuestionSubmitMapper;
import com.chwww924.chwwwBackend.model.entity.Question;
import com.chwww924.chwwwBackend.model.entity.QuestionSubmit;
import com.chwww924.chwwwBackend.model.entity.User;
import org.springframework.stereotype.Service;

/**
* @author ASUS
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2024-05-05 21:14:14
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService {

    private QuestionService questionService;

    public QuestionSubmitServiceImpl(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        String lauguage = questionSubmitAddRequest.getLanguage();
        if (lauguage == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 判断实体是否存在，根据类别获取实体
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交

        long userId = loginUser.getId();
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setUserId(userId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(questionSubmit.getLanguage());
        // 每个用户串行提交
        // 锁必须要包裹住事务方法
//        QuestionSubmitService questionThumbService = (QuestionSubmitService) AopContext.currentProxy();
//        synchronized (String.valueOf(userId).intern()) {
//            return questionThumbService.doQuestionSubmitInner(userId, questionId);
//        }
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudegeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        return questionSubmit.getId();

    }

//    @Override
//    public int doQuestionSubmitInner(long userId, long questionId) {
//        QuestionSubmit questionThumb = new QuestionSubmit();
//        questionThumb.setUserId(userId);
//        questionThumb.setQuestionId(questionId);
//        QueryWrapper<QuestionSubmit> thumbQueryWrapper = new QueryWrapper<>(questionThumb);
//        QuestionSubmit oldQuestionSubmit = this.getOne(thumbQueryWrapper);
//        boolean result;
//        // 已提交
//        if (oldQuestionSubmit != null) {
//            result = this.remove(thumbQueryWrapper);
//            if (result) {
//                // 提交数 - 1
//                result = questionService.update()
//                        .eq("id", questionId)
//                        .gt("thumbNum", 0)
//                        .setSql("thumbNum = thumbNum - 1")
//                        .update();
//                return result ? -1 : 0;
//            } else {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//            }
//        } else {
//            // 未提交
//            result = this.save(questionThumb);
//            if (result) {
//                // 提交数 + 1
//                result = questionService.update()
//                        .eq("id", questionId)
//                        .setSql("thumbNum = thumbNum + 1")
//                        .update();
//                return result ? 1 : 0;
//            } else {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//            }
//        }
//    }
}




