package com.chwww924.chwwwBackend.model.dto.coach;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 添加组成员请求
 */
@Data
public class GroupMemberAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学员ID列表（支持批量添加）
     */
    private List<Long> studentIds;
}




