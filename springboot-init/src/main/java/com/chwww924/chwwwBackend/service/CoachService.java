package com.chwww924.chwwwBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chwww924.chwwwBackend.model.dto.coach.GroupAddRequest;
import com.chwww924.chwwwBackend.model.dto.coach.GroupMemberAddRequest;
import com.chwww924.chwwwBackend.model.dto.coach.GroupUpdateRequest;
import com.chwww924.chwwwBackend.model.dto.coach.UserSearchRequest;
import com.chwww924.chwwwBackend.model.entity.Group;
import com.chwww924.chwwwBackend.model.entity.GroupMember;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.model.vo.UserVO;

import java.util.List;

/**
 * 教练员服务接口
 */
public interface CoachService extends IService<User> {

    /**
     * 搜索无主用户（coach_id 为 null）
     */
    List<UserVO> searchUnassignedUsers(UserSearchRequest request);

    /**
     * 绑定用户到当前教练
     */
    boolean bindUser(Long studentId, Long coachId);

    /**
     * 查看我的学员列表
     */
    List<UserVO> getMyStudents(Long coachId);

    /**
     * 解绑用户
     */
    boolean unbindUser(Long studentId, Long coachId);

    /**
     * 创建分组
     */
    Long createGroup(GroupAddRequest request, Long coachId);

    /**
     * 查询我的分组列表（包含成员数量）
     */
    List<Group> getMyGroups(Long coachId);

    /**
     * 更新分组
     */
    boolean updateGroup(GroupUpdateRequest request, Long coachId);

    /**
     * 删除分组
     */
    boolean deleteGroup(Long groupId, Long coachId);

    /**
     * 向分组添加成员
     */
    boolean addGroupMembers(Long groupId, GroupMemberAddRequest request, Long coachId);

    /**
     * 从分组移除成员
     */
    boolean removeGroupMember(Long groupId, Long studentId, Long coachId);

    /**
     * 查询分组成员列表
     */
    List<UserVO> getGroupMembers(Long groupId, Long coachId);

    /**
     * 校验分组是否属于当前教练
     */
    boolean isGroupOwner(Long groupId, Long coachId);
}




