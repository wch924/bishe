package com.chwww924.chwwwBackend.model.dto.training;

import lombok.Data;

import java.io.Serializable;

/**
 * 训练数据上传请求
 */
@Data
public class TrainingUploadRequest implements Serializable {
    
    /**
     * 会话标题
     */
    private String title;

    private static final long serialVersionUID = 1L;
}
