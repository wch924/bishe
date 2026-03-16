package com.chwww924.chwwwBackend.model.dto.coach;

import lombok.Data;

import java.io.Serializable;

/**
 * 搜索无主用户请求
 */
@Data
public class UserSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户账号（模糊搜索）
     */
    private String userAccount;

    /**
     * 用户名称（模糊搜索）
     */
    private String userName;
}




