package com.chwww924.chwwwBackend.model.dto.ecg;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * ECG数据添加请求
 */
@Data
public class EcgAddRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * ECG数据点列表
     */
    private List<EcgPoint> points;

    private static final long serialVersionUID = 1L;
}