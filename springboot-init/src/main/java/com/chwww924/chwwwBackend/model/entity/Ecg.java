package com.chwww924.chwwwBackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value ="ecg_data")
@Data
public class Ecg implements Serializable {
    private Long id;
    private Long sessionId;
    private LocalDateTime timestamp;
    private BigDecimal ecgValue;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
