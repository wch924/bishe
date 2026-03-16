package com.chwww924.chwwwBackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 训练会话实体
 * @TableName training_session
 */
@TableName(value = "training_session")
@Data
public class TrainingSession implements Serializable {
    
    /**
     * 会话ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 心率数据文件路径
     */
    @TableField("heart_rate_file_path")
    private String heartRateFilePath;

    /**
     * 运动数据文件路径
     */
    @TableField("motion_file_path")
    private String motionFilePath;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
