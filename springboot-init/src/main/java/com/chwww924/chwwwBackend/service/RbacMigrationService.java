package com.chwww924.chwwwBackend.service;

/**
 * RBAC 数据迁移服务接口
 * 用于将旧的 userRole 系统迁移到 RBAC 系统
 */
public interface RbacMigrationService {
    
    /**
     * 执行数据迁移
     * 将 userRole='admin' 的用户自动分配 SuperAdmin 角色
     * 将 userRole='user' 的用户自动分配 User 角色
     * 
     * @return 迁移的用户数量
     */
    int migrateUserRoles();
    
    /**
     * 验证迁移结果
     * 检查是否有 admin 用户没有 SuperAdmin 角色
     * 
     * @return 未迁移的 admin 用户数量
     */
    int validateMigration();
}




