package com.chwww924.chwwwBackend.model.dto.coach;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新分组请求
 */
@Data
public class GroupUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分组ID
     */
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组描述
     */
    private String description;
}




