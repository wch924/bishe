package com.chwww924.chwwwBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.mapper.GroupMapper;
import com.chwww924.chwwwBackend.mapper.GroupMemberMapper;
import com.chwww924.chwwwBackend.mapper.UserMapper;
import com.chwww924.chwwwBackend.model.dto.coach.GroupAddRequest;
import com.chwww924.chwwwBackend.model.dto.coach.GroupMemberAddRequest;
import com.chwww924.chwwwBackend.model.dto.coach.GroupUpdateRequest;
import com.chwww924.chwwwBackend.model.dto.coach.UserSearchRequest;
import com.chwww924.chwwwBackend.model.entity.Group;
import com.chwww924.chwwwBackend.model.entity.GroupMember;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.model.vo.UserVO;
import com.chwww924.chwwwBackend.service.CoachService;
import com.chwww924.chwwwBackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练员服务实现
 */
@Slf4j
@Service
public class CoachServiceImpl extends ServiceImpl<UserMapper, User> implements CoachService {

    @Resource
    private UserService userService;

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private GroupMemberMapper groupMemberMapper;

    @Override
    public List<UserVO> searchUnassignedUsers(UserSearchRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 只查询 coach_id 为 null 的用户（无主用户）
        queryWrapper.isNull("coach_id");
        queryWrapper.eq("isDelete", 0);
        
        // 模糊搜索
        if (StringUtils.isNotBlank(request.getUserAccount())) {
            queryWrapper.like("userAccount", request.getUserAccount());
        }
        if (StringUtils.isNotBlank(request.getUserName())) {
            queryWrapper.like("userName", request.getUserName());
        }
        
        List<User> users = this.list(queryWrapper);
        return users.stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindUser(Long studentId, Long coachId) {
        if (studentId == null || coachId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 再次检查该用户当前是否确认为无教练状态，防止并发冲突
        User user = this.getById(studentId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        if (user.getCoachId() != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已经绑定其他教练");
        }
        
        // 绑定用户
        user.setCoachId(coachId);
        return this.updateById(user);
    }

    @Override
    public List<UserVO> getMyStudents(Long coachId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coach_id", coachId);
        queryWrapper.eq("isDelete", 0);
        
        List<User> users = this.list(queryWrapper);
        return users.stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindUser(Long studentId, Long coachId) {
        if (studentId == null || coachId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 检查用户是否属于当前教练
        User user = this.getById(studentId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        if (!coachId.equals(user.getCoachId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该用户不是您的学员");
        }
        
        // 解绑用户（将 coach_id 设为 null）
        // 使用UpdateWrapper显式设置null值，因为MyBatis Plus默认忽略null字段
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", studentId)
                     .set("coach_id", null);
        boolean result = this.update(updateWrapper);
        
        // 级联删除：从该教练的所有分组中移除该学员
        if (result) {
            groupMemberMapper.deleteByStudentId(studentId);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(GroupAddRequest request, Long coachId) {
        if (request == null || StringUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分组名称不能为空");
        }
        
        Group group = new Group();
        group.setCoachId(coachId);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        
        groupMapper.insert(group);
        return group.getId();
    }

    @Override
    public List<Group> getMyGroups(Long coachId) {
        List<Group> groups = groupMapper.selectGroupsByCoachId(coachId);
        // 为每个分组添加成员数量
        for (Group group : groups) {
            QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("group_id", group.getId());
            long memberCount = groupMemberMapper.selectCount(queryWrapper);
            group.setMemberCount(memberCount);
        }
        return groups;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGroup(GroupUpdateRequest request, Long coachId) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 校验分组是否属于当前教练
        if (!isGroupOwner(request.getId(), coachId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该分组");
        }
        
        Group group = new Group();
        group.setId(request.getId());
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        
        return groupMapper.updateById(group) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroup(Long groupId, Long coachId) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 校验分组是否属于当前教练
        if (!isGroupOwner(groupId, coachId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该分组");
        }
        
        // 删除分组（级联删除 GroupMembers 中的关联记录）
        return groupMapper.deleteById(groupId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addGroupMembers(Long groupId, GroupMemberAddRequest request, Long coachId) {
        if (groupId == null || request == null || request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 1. 校验分组是否属于当前教练
        if (!isGroupOwner(groupId, coachId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该分组");
        }
        
        // 2. 遍历 student_ids，严格校验每个学生是否已经绑定在当前教练名下
        List<Long> studentIds = request.getStudentIds();
        for (Long studentId : studentIds) {
            User student = this.getById(studentId);
            if (student == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "学员不存在，ID: " + studentId);
            }
            if (!coachId.equals(student.getCoachId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户不是您的学员，ID: " + studentId);
            }
        }
        
        // 3. 批量添加成员
        List<GroupMember> members = new ArrayList<>();
        for (Long studentId : studentIds) {
            // 检查是否已存在
            QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("group_id", groupId);
            queryWrapper.eq("student_id", studentId);
            long count = groupMemberMapper.selectCount(queryWrapper);
            if (count == 0) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setStudentId(studentId);
                members.add(member);
            }
        }
        
        if (!members.isEmpty()) {
            for (GroupMember member : members) {
                groupMemberMapper.insert(member);
            }
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeGroupMember(Long groupId, Long studentId, Long coachId) {
        if (groupId == null || studentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 校验分组是否属于当前教练
        if (!isGroupOwner(groupId, coachId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该分组");
        }
        
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("student_id", studentId);
        
        return groupMemberMapper.delete(queryWrapper) > 0;
    }

    @Override
    public List<UserVO> getGroupMembers(Long groupId, Long coachId) {
        if (groupId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 校验分组是否属于当前教练
        if (!isGroupOwner(groupId, coachId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看该分组");
        }
        
        // 查询分组成员
        QueryWrapper<GroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        
        if (members.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> studentIds = members.stream()
                .map(GroupMember::getStudentId)
                .collect(Collectors.toList());
        
        List<User> users = this.listByIds(studentIds);
        return users.stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isGroupOwner(Long groupId, Long coachId) {
        if (groupId == null || coachId == null) {
            return false;
        }
        
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            return false;
        }
        
        return coachId.equals(group.getCoachId());
    }
}

