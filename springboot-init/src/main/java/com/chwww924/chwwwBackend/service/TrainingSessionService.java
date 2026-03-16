package com.chwww924.chwwwBackend.service;

import com.chwww924.chwwwBackend.model.vo.TrainingSessionVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 训练会话服务接口
 */
public interface TrainingSessionService {

    /**
     * 上传训练数据
     * 
     * @param title 会话标题
     * @param heartRateFile 心率数据文件
     * @param motionFile 运动数据文件
     * @param userId 用户ID
     * @return 会话ID
     */
    Long uploadTrainingData(String title, MultipartFile heartRateFile, MultipartFile motionFile, Long userId);

    /**
     * 获取当前用户的训练会话列表
     * 
     * @param userId 用户ID
     * @return 训练会话列表
     */
    List<TrainingSessionVO> getMyTrainingSessions(Long userId);

    /**
     * 获取教练的学员训练会话列表
     * 
     * @param coachId 教练ID
     * @return 训练会话列表（包含学员信息）
     */
    List<TrainingSessionVO> getCoachStudentSessions(Long coachId);
}
