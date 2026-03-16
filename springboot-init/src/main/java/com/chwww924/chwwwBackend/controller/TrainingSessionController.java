package com.chwww924.chwwwBackend.controller;

import com.chwww924.chwwwBackend.common.BaseResponse;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.common.ResultUtils;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.model.vo.TrainingSessionVO;
import com.chwww924.chwwwBackend.service.TrainingSessionService;
import com.chwww924.chwwwBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 训练会话接口
 */
@RestController
@RequestMapping("/training")
@Slf4j
public class TrainingSessionController {

    @Resource
    private TrainingSessionService trainingSessionService;

    @Resource
    private UserService userService;

    /**
     * 上传训练数据
     *
     * @param title 会话标题
     * @param heartRateFile 心率数据文件
     * @param motionFile 运动数据文件
     * @param request HTTP请求
     * @return 会话ID
     */
    @PostMapping("/upload")
    public BaseResponse<Long> uploadTrainingData(
            @RequestParam("title") String title,
            @RequestParam("heartRateFile") MultipartFile heartRateFile,
            @RequestParam("motionFile") MultipartFile motionFile,
            HttpServletRequest request) {
        
        // 参数校验
        if (StringUtils.isBlank(title)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话标题不能为空");
        }
        if (heartRateFile == null || heartRateFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "心率数据文件不能为空");
        }
        if (motionFile == null || motionFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "运动数据文件不能为空");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 调用服务层上传数据
        Long sessionId = trainingSessionService.uploadTrainingData(title, heartRateFile, motionFile, loginUser.getId());
        
        return ResultUtils.success(sessionId);
    }

    /**
     * 获取我的训练会话列表
     *
     * @param request HTTP请求
     * @return 训练会话列表
     */
    @GetMapping("/my-list")
    public BaseResponse<List<TrainingSessionVO>> getMyTrainingSessions(HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 查询训练会话列表
        List<TrainingSessionVO> sessions = trainingSessionService.getMyTrainingSessions(loginUser.getId());
        
        return ResultUtils.success(sessions);
    }

    /**
     * 教练查看学员的训练会话列表
     *
     * @param request HTTP请求
     * @return 学员训练会话列表
     */
    @GetMapping("/coach/student-sessions")
    public BaseResponse<List<TrainingSessionVO>> getCoachStudentSessions(HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 调用服务层查询（服务层会验证教练权限）
        List<TrainingSessionVO> sessions = trainingSessionService.getCoachStudentSessions(loginUser.getId());
        
        return ResultUtils.success(sessions);
    }
}
