package com.chwww924.chwwwBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.mapper.TrainingSessionMapper;
import com.chwww924.chwwwBackend.model.entity.TrainingSession;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.model.vo.TrainingSessionVO;
import com.chwww924.chwwwBackend.model.vo.UserVO;
import com.chwww924.chwwwBackend.service.FileStorageService;
import com.chwww924.chwwwBackend.service.RbacService;
import com.chwww924.chwwwBackend.service.TrainingSessionService;
import com.chwww924.chwwwBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练会话服务实现
 */
@Service
@Slf4j
public class TrainingSessionServiceImpl implements TrainingSessionService {

    @Resource
    private TrainingSessionMapper trainingSessionMapper;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private UserService userService;

    @Resource
    private RbacService rbacService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadTrainingData(String title, MultipartFile heartRateFile, MultipartFile motionFile, Long userId) {
        // 参数校验
        if (StringUtils.isBlank(title)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话标题不能为空");
        }
        
        // 文件校验
        fileStorageService.validateFile(heartRateFile);
        fileStorageService.validateFile(motionFile);

        // 创建训练会话记录（先插入数据库以获取ID）
        TrainingSession trainingSession = new TrainingSession();
        trainingSession.setUserId(userId);
        trainingSession.setTitle(title);
        // 临时设置文件路径，后续更新
        trainingSession.setHeartRateFilePath("pending");
        trainingSession.setMotionFilePath("pending");

        int insertResult = trainingSessionMapper.insert(trainingSession);
        if (insertResult <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建训练会话失败");
        }

        Long sessionId = trainingSession.getId();
        
        try {
            // 保存文件（使用生成的sessionId作为目录名）
            String heartRateFilePath = fileStorageService.saveFile(heartRateFile, userId, sessionId, "heart_rate.csv");
            String motionFilePath = fileStorageService.saveFile(motionFile, userId, sessionId, "motion.csv");

            // 更新文件路径
            trainingSession.setHeartRateFilePath(heartRateFilePath);
            trainingSession.setMotionFilePath(motionFilePath);
            
            int updateResult = trainingSessionMapper.updateById(trainingSession);
            if (updateResult <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新文件路径失败");
            }

            log.info("训练数据上传成功: userId={}, sessionId={}", userId, sessionId);
            return sessionId;
            
        } catch (Exception e) {
            // 如果文件保存或数据库更新失败，删除已上传的文件
            log.error("训练数据上传失败，执行文件回滚: userId={}, sessionId={}", userId, sessionId, e);
            fileStorageService.deleteSessionFiles(userId, sessionId);
            throw e;
        }
    }

    @Override
    public List<TrainingSessionVO> getMyTrainingSessions(Long userId) {
        // 查询当前用户的所有训练会话
        QueryWrapper<TrainingSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderByDesc("create_time");

        List<TrainingSession> sessions = trainingSessionMapper.selectList(queryWrapper);
        
        // 转换为VO
        return sessions.stream().map(session -> {
            TrainingSessionVO vo = new TrainingSessionVO();
            BeanUtils.copyProperties(session, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TrainingSessionVO> getCoachStudentSessions(Long coachId) {
        // 验证用户是否拥有教练角色
        if (!rbacService.hasRole(coachId, "coach")) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您没有教练权限");
        }

        // 获取教练的所有学员（复用现有方法）
        List<UserVO> assignedStudents = userService.getAssignedUsersByCoachId(coachId);
        
        if (assignedStudents == null || assignedStudents.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取学员ID列表
        List<Long> studentIds = assignedStudents.stream()
                .map(UserVO::getId)
                .collect(Collectors.toList());

        // 创建学员ID到姓名的映射
        Map<Long, String> studentNameMap = new HashMap<>();
        for (UserVO student : assignedStudents) {
            studentNameMap.put(student.getId(), student.getUserName());
        }

        // 查询这些学员的所有训练会话
        QueryWrapper<TrainingSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", studentIds);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderByDesc("create_time");

        List<TrainingSession> sessions = trainingSessionMapper.selectList(queryWrapper);

        // 转换为VO并添加学员姓名
        return sessions.stream().map(session -> {
            TrainingSessionVO vo = new TrainingSessionVO();
            BeanUtils.copyProperties(session, vo);
            // 添加学员姓名
            vo.setUserName(studentNameMap.get(session.getUserId()));
            return vo;
        }).collect(Collectors.toList());
    }
}
