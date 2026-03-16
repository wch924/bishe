package com.yupi.yuaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YuManusTest {

    @Resource
    private YuManus yuManus;

    @Test
    public void run() {
        String userPrompt = """
                给我简单的科普一下运动负荷吧。然后把科普内容保存为pdf
                """;
        String answer = yuManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}