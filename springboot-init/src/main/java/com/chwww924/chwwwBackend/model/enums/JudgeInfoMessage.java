package com.chwww924.chwwwBackend.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public enum JudgeInfoMessage {

    USER_AVATAR("用户头像", "user_avatar"),

    ACCEPTED("Accepted", "成功"),
    WRONG_ANSWER("Wrong Answer", "答案错误"),
    COMPILE_ERROR("Compile error", "编译错误"),
    Memory_Limit_Exceeded("Memory Limit Exceeded", "内存溢出"),
    TIME_LIMIT_ERROR("Time Limit Error", "超时"),
    PRESENTATION_ERROR("Presentation Error", "展示错误"),
    WAITING("Waiting","等待中"),
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded","输出溢出"),
    DANGEROUS_OPERATION("dangerous Operation", "危险操作"),
    RUNTIME_ERROR("Runtime Error", "运行错误"),
    SYSTEM_ERROR("System Error", "系统错误");

    private final String text;

    private final String value;

    JudgeInfoMessage(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoMessage getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeInfoMessage anEnum : JudgeInfoMessage.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
