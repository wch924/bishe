package com.chwww924.chwwwBackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 训练会话视图对象
 */
@Data
public class TrainingSessionVO implements Serializable {
    
    /**
     * 会话ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称（用于教练查看学员数据时显示）
     */
    private String userName;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
