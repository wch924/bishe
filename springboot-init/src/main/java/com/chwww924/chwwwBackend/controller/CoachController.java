package com.chwww924.chwwwBackend.controller;

import com.chwww924.chwwwBackend.annotation.RequirePermission;
import com.chwww924.chwwwBackend.common.BaseResponse;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.common.ResultUtils;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.model.dto.coach.GroupAddRequest;
import com.chwww924.chwwwBackend.model.dto.coach.GroupMemberAddRequest;
import com.chwww924.chwwwBackend.model.dto.coach.GroupUpdateRequest;
import com.chwww924.chwwwBackend.model.dto.coach.UserSearchRequest;
import com.chwww924.chwwwBackend.model.entity.Group;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.model.vo.UserVO;
import com.chwww924.chwwwBackend.service.CoachService;
import com.chwww924.chwwwBackend.service.RbacService;
import com.chwww924.chwwwBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 教练员接口
 */
@RestController
@RequestMapping("/coach")
@Slf4j
public class CoachController {

    @Resource
    private CoachService coachService;

    @Resource
    private UserService userService;

    @Resource
    private RbacService rbacService;

    /**
     * 搜索无主用户
     */
    @GetMapping("/users/search")
    @RequirePermission("user:read:assigned")
    public BaseResponse<List<UserVO>> searchUnassignedUsers(UserSearchRequest request) {
        if (request == null) {
            request = new UserSearchRequest();
        }
        List<UserVO> users = coachService.searchUnassignedUsers(request);
        return ResultUtils.success(users);
    }

    /**
     * 绑定用户
     */
    @PostMapping("/users/bind")
    @RequirePermission("user:update:assigned")
    public BaseResponse<Boolean> bindUser(@RequestParam Long studentId, HttpServletRequest httpRequest) {
        if (studentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = coachService.bindUser(studentId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 查看我的学员列表
     */
    @GetMapping("/my-students")
    @RequirePermission("user:read:assigned")
    public BaseResponse<List<UserVO>> getMyStudents(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        List<UserVO> students = coachService.getMyStudents(loginUser.getId());
        return ResultUtils.success(students);
    }

    /**
     * 解绑用户
     */
    @PostMapping("/users/unbind")
    @RequirePermission("user:update:assigned")
    public BaseResponse<Boolean> unbindUser(@RequestParam Long studentId, HttpServletRequest httpRequest) {
        if (studentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = coachService.unbindUser(studentId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 创建分组
     */
    @PostMapping("/groups")
    @RequirePermission("user:read:assigned")
    public BaseResponse<Long> createGroup(@RequestBody GroupAddRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        Long groupId = coachService.createGroup(request, loginUser.getId());
        return ResultUtils.success(groupId);
    }

    /**
     * 查询我的分组列表
     */
    @GetMapping("/groups")
    @RequirePermission("user:read:assigned")
    public BaseResponse<List<Group>> getMyGroups(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        List<Group> groups = coachService.getMyGroups(loginUser.getId());
        return ResultUtils.success(groups);
    }

    /**
     * 修改分组
     */
    @PutMapping("/groups")
    @RequirePermission("user:read:assigned")
    public BaseResponse<Boolean> updateGroup(@RequestBody GroupUpdateRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = coachService.updateGroup(request, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 删除分组
     */
    @DeleteMapping("/groups/{id}")
    @RequirePermission("user:read:assigned")
    public BaseResponse<Boolean> deleteGroup(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = coachService.deleteGroup(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 向分组添加成员
     */
    @PostMapping("/groups/{groupId}/members")
    @RequirePermission("user:read:assigned")
    public BaseResponse<Boolean> addGroupMembers(@PathVariable Long groupId,
                                                 @RequestBody GroupMemberAddRequest request,
                                                 HttpServletRequest httpRequest) {
        if (groupId == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = coachService.addGroupMembers(groupId, request, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 从分组移除成员
     */
    @DeleteMapping("/groups/{groupId}/members/{studentId}")
    @RequirePermission("user:read:assigned")
    public BaseResponse<Boolean> removeGroupMember(@PathVariable Long groupId,
                                                   @PathVariable Long studentId,
                                                   HttpServletRequest httpRequest) {
        if (groupId == null || studentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = coachService.removeGroupMember(groupId, studentId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 查询分组成员列表
     */
    @GetMapping("/groups/{groupId}/members")
    @RequirePermission("user:read:assigned")
    public BaseResponse<List<UserVO>> getGroupMembers(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User loginUser = userService.getLoginUser(httpRequest);
        List<UserVO> members = coachService.getGroupMembers(groupId, loginUser.getId());
        return ResultUtils.success(members);
    }
}

