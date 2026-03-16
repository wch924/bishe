package com.chwww924.chwwwBackend.model.dto.ecg;

import com.chwww924.chwwwBackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ECG数据查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EcgQueryRequest extends PageRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    private static final long serialVersionUID = 1L;
}