package com.chwww924.chwwwBackend.model.dto.ecg;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * ECG数据点
 */
@Data
public class EcgPoint implements Serializable {
    /**
     * Unix时间戳（秒.小数部分）
     */
    private Double timestamp;

    /**
     * ECG电压值（单位：µV）
     */
    private Integer ecg_value;

    private static final long serialVersionUID = 1L;
}