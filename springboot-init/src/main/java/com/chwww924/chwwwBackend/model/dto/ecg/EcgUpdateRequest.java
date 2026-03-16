package com.chwww924.chwwwBackend.model.dto.ecg;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * ECG数据更新请求
 */
@Data
public class EcgUpdateRequest implements Serializable {
    /**
     * 记录ID
     */
    private Long id;

    /**
     * 新的ECG数据点
     */
    private List<EcgPoint> points;

    private static final long serialVersionUID = 1L;
}