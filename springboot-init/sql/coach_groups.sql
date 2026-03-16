-- Coach Groups Management Tables

USE oj_db;

-- 1. Create Groups table
CREATE TABLE IF NOT EXISTS `groups` (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    coach_id bigint NOT NULL,
    name varchar(100) NOT NULL,
    description varchar(500) NULL,
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    isDelete tinyint DEFAULT 0 NOT NULL,
    INDEX idx_coach_id (coach_id),
    FOREIGN KEY (coach_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB COLLATE = utf8mb4_unicode_ci;

-- 2. Create GroupMembers table
CREATE TABLE IF NOT EXISTS `group_members` (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    group_id bigint NOT NULL,
    student_id bigint NOT NULL,
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_group_student (group_id, student_id),
    INDEX idx_group_id (group_id),
    INDEX idx_student_id (student_id),
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB COLLATE = utf8mb4_unicode_ci;
