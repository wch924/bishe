package com.chwww924.chwwwBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chwww924.chwwwBackend.model.entity.Ecg;

import java.util.List;

public interface EcgService extends IService<Ecg> {
    void saveEcgData(Long sessionId, List<Ecg> dataPoints);
}
