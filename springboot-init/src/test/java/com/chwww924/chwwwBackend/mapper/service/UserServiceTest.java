package com.chwww924.chwwwBackend.mapper.service;

import javax.annotation.Resource;

import com.chwww924.chwwwBackend.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户服务测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void userRegister() {
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        String userName = "nihao";
        try {
            long result = userService.userRegister(userAccount, userPassword, checkPassword, userName);
            Assertions.assertEquals(-1, result);
            userAccount = "yu";
            result = userService.userRegister(userAccount, userPassword, checkPassword,userName);
            Assertions.assertEquals(-1, result);
        } catch (Exception e) {

        }
    }
}
