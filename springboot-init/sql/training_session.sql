-- 训练会话数据表
CREATE TABLE IF NOT EXISTS training_session (
    id BIGINT AUTO_INCREMENT COMMENT '会话ID' PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(256) NOT NULL COMMENT '会话标题',
    heart_rate_file_path VARCHAR(512) NOT NULL COMMENT '心率数据文件路径',
    motion_file_path VARCHAR(512) NOT NULL COMMENT '运动数据文件路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT '训练会话数据表' COLLATE = utf8mb4_unicode_ci;
