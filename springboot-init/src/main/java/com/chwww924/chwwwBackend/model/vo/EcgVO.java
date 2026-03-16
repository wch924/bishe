package com.chwww924.chwwwBackend.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chwww924.chwwwBackend.model.dto.question.JudgeConfig;
import com.chwww924.chwwwBackend.model.entity.Ecg;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName(value ="ecg_data")
@Data
public class EcgVO implements Serializable {
    private Long id;
    private Long sessionId;
    private LocalDateTime timestamp;
    private BigDecimal ecgValue;
    private UserVO userVO;
    private EcgVO ecgVO;
    public static Ecg voToObj(EcgVO ecgVO) {
        if (ecgVO == null) {
            return null;
        }
        Ecg ecg = new Ecg();
        BeanUtils.copyProperties(ecgVO, ecg);
        return ecg;
    }
    public static EcgVO objToVo(Ecg ecg) {
        if (ecg == null) {
            return null;
        }
        EcgVO ecgVO = new EcgVO();
        BeanUtils.copyProperties(ecg, ecgVO);
        return ecgVO;
    }
    private static final long serialVersionUID = 1L;
}
