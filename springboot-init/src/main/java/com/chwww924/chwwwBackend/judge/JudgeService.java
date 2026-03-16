package com.chwww924.chwwwBackend.judge;

import com.chwww924.chwwwBackend.model.entity.QuestionSubmit;
import com.chwww924.chwwwBackend.model.vo.QuestionVO;

public interface JudgeService {
    QuestionSubmit doJudge(long questionSubmitId);
}
