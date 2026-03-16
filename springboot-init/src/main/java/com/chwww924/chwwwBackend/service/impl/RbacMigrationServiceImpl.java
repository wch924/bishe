package com.chwww924.chwwwBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chwww924.chwwwBackend.mapper.UserMapper;
import com.chwww924.chwwwBackend.mapper.UserRoleMapper;
import com.chwww924.chwwwBackend.model.entity.User;
import com.chwww924.chwwwBackend.model.entity.UserRole;
import com.chwww924.chwwwBackend.service.RbacMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * RBAC 数据迁移服务实现
 */
@Slf4j
@Service
public class RbacMigrationServiceImpl implements RbacMigrationService {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private UserRoleMapper userRoleMapper;
    
    // 角色ID常量（根据实际数据库中的角色ID调整）
    private static final Long ROLE_SUPER_ADMIN = 1L;  // SuperAdmin
    private static final Long ROLE_COACH = 2L;         // Coach
    private static final Long ROLE_USER = 3L;          // User
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int migrateUserRoles() {
        log.info("开始执行 RBAC 数据迁移...");
        int migratedCount = 0;
        
        // 1. 迁移 admin 用户到 SuperAdmin 角色
        QueryWrapper<User> adminQuery = new QueryWrapper<>();
        adminQuery.eq("userRole", "admin");
        adminQuery.eq("isDelete", 0);
        List<User> adminUsers = userMapper.selectList(adminQuery);
        
        for (User user : adminUsers) {
            // 检查是否已经分配了 SuperAdmin 角色
            QueryWrapper<UserRole> roleQuery = new QueryWrapper<>();
            roleQuery.eq("user_id", user.getId());
            roleQuery.eq("role_id", ROLE_SUPER_ADMIN);
            Long count = userRoleMapper.selectCount(roleQuery);
            
            if (count == 0) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(ROLE_SUPER_ADMIN);
                userRole.setAssignedBy(1L); // 系统分配
                userRole.setCreateTime(new Date());
                userRole.setUpdateTime(new Date());
                userRoleMapper.insert(userRole);
                migratedCount++;
                log.info("为用户 {} ({}) 分配 SuperAdmin 角色", user.getUserAccount(), user.getId());
            }
        }
        
        // 2. 迁移 user 用户到 User 角色
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("userRole", "user");
        userQuery.eq("isDelete", 0);
        List<User> normalUsers = userMapper.selectList(userQuery);
        
        for (User user : normalUsers) {
            // 检查是否已经分配了 User 角色
            QueryWrapper<UserRole> roleQuery = new QueryWrapper<>();
            roleQuery.eq("user_id", user.getId());
            roleQuery.eq("role_id", ROLE_USER);
            Long count = userRoleMapper.selectCount(roleQuery);
            
            if (count == 0) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(ROLE_USER);
                userRole.setAssignedBy(1L); // 系统分配
                userRole.setCreateTime(new Date());
                userRole.setUpdateTime(new Date());
                userRoleMapper.insert(userRole);
                migratedCount++;
                log.info("为用户 {} ({}) 分配 User 角色", user.getUserAccount(), user.getId());
            }
        }
        
        // 3. 清理：移除教练员的普通用户角色（因为教练员已包含普通用户的所有权限）
        QueryWrapper<UserRole> coachUserRoleQuery = new QueryWrapper<>();
        coachUserRoleQuery.in("user_id", 
            userRoleMapper.selectObjs(new QueryWrapper<UserRole>()
                .select("DISTINCT user_id")
                .eq("role_id", ROLE_COACH))
        );
        coachUserRoleQuery.eq("role_id", ROLE_USER);
        int removedCount = userRoleMapper.delete(coachUserRoleQuery);
        if (removedCount > 0) {
            log.info("清理了 {} 个教练员的普通用户角色（因为教练员已包含普通用户的所有权限）", removedCount);
        }
        
        log.info("RBAC 数据迁移完成，共迁移 {} 个用户", migratedCount);
        return migratedCount;
    }
    
    @Override
    public int validateMigration() {
        log.info("开始验证 RBAC 数据迁移结果...");
        int unmigratedCount = 0;
        
        // 检查是否有 admin 用户没有 SuperAdmin 角色
        QueryWrapper<User> adminQuery = new QueryWrapper<>();
        adminQuery.eq("userRole", "admin");
        adminQuery.eq("isDelete", 0);
        List<User> adminUsers = userMapper.selectList(adminQuery);
        
        for (User user : adminUsers) {
            QueryWrapper<UserRole> roleQuery = new QueryWrapper<>();
            roleQuery.eq("user_id", user.getId());
            roleQuery.eq("role_id", ROLE_SUPER_ADMIN);
            Long count = userRoleMapper.selectCount(roleQuery);
            
            if (count == 0) {
                unmigratedCount++;
                log.warn("用户 {} ({}) 是 admin 但没有 SuperAdmin 角色", user.getUserAccount(), user.getId());
            }
        }
        
        log.info("验证完成，发现 {} 个未迁移的 admin 用户", unmigratedCount);
        return unmigratedCount;
    }
}

