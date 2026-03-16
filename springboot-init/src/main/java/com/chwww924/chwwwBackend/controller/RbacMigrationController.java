package com.chwww924.chwwwBackend.controller;

import com.chwww924.chwwwBackend.annotation.AuthCheck;
import com.chwww924.chwwwBackend.common.BaseResponse;
import com.chwww924.chwwwBackend.common.ResultUtils;
import com.chwww924.chwwwBackend.constant.UserConstant;
import com.chwww924.chwwwBackend.service.RbacMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * RBAC 数据迁移控制器
 * 用于执行数据迁移和验证
 */
@Slf4j
@RestController
@RequestMapping("/rbac/migration")
public class RbacMigrationController {
    
    @Resource
    private RbacMigrationService rbacMigrationService;
    
    /**
     * 执行数据迁移
     * 只有超级管理员可以执行
     */
    @PostMapping("/execute")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> executeMigration() {
        int migratedCount = rbacMigrationService.migrateUserRoles();
        return ResultUtils.success(migratedCount);
    }
    
    /**
     * 验证迁移结果
     * 只有超级管理员可以执行
     */
    @PostMapping("/validate")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> validateMigration() {
        int unmigratedCount = rbacMigrationService.validateMigration();
        return ResultUtils.success(unmigratedCount);
    }
}




