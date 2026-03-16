# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>

-- 创建库
create database if not exists oj_db;

-- 切换库
use oj_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) ENGINE=InnoDB comment '用户' collate = utf8mb4_unicode_ci;

-- 会话表
create table if not exists sessions (
    session_id  bigint auto_increment comment 'id' primary key,  -- 采集会话唯一ID
    user_id bigint NOT NULL,                      -- 关联的用户ID
    device_id VARCHAR(50),                     -- 设备标识（如Polar H10的序列号）
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 采集开始时间
    end_time TIMESTAMP,                        -- 采集结束时间（可选）
    FOREIGN KEY (user_id) REFERENCES oj_db.user(id)   -- 外键约束
) ENGINE=InnoDB comment '会话' collate = utf8mb4_unicode_ci;

-- HR表
CREATE TABLE if not exists hr_data (
                         hr_id INT AUTO_INCREMENT PRIMARY KEY,  -- 心率数据唯一ID
                         session_id bigint NOT NULL,               -- 关联的采集会话ID
                         timestamp TIMESTAMP(3) NOT NULL,       -- 精确到毫秒的时间戳
                         heart_rate INT NOT NULL,               -- 心率值（BPM）
                         FOREIGN KEY (session_id) REFERENCES sessions(session_id)  -- 外键约束
);

-- ECG表
CREATE TABLE if not exists ecg_data (
                          ecg_id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- ECG数据唯一ID（大数据量建议用BIGINT）
                          session_id BIGINT NOT NULL,                   -- 关联的采集会话ID
                          timestamp DECIMAL(17,7) NOT NULL,           -- 精确到微秒的时间戳
                          ecg_value DECIMAL(8, 2) NOT NULL,          -- ECG电压值（单位：mV或μV）
                          FOREIGN KEY (session_id) REFERENCES sessions(session_id)  -- 外键约束
);
-- 题目表
create table if not exists question
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    answer     text                               null comment '题目答案',
    submitNum  int  default 0                     not null comment '题目提交数量',
    accepted   int default 0                      not null comment '题目通过数量',
    judgeCase  text                               null comment '判题用例（JSON数组）',
    judgeConfig  text                             null comment '判题配置（JSON对象）',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
    ) comment '题目' collate = utf8mb4_unicode_ci;

-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language    varchar(128)                      not null comment '编程语言',
    code        text                              not null comment '用户代码',
    judegeInfo  text                              not null comment '判题信息（JSON对象）',
    status      int     default 0                 not null comment '判题状态（0 -待判题、1 -判题中、 2 -判题成功、3 -判题失败）',
    questionId     bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_questionId (questionId),
    index idx_userId (userId)
    ) comment '题目提交';

-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';
