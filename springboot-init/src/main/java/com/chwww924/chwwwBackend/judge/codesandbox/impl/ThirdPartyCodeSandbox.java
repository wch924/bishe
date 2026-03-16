package com.chwww924.chwwwBackend.judge.codesandbox.impl;

import com.chwww924.chwwwBackend.judge.codesandbox.CodeSandbox;
import com.chwww924.chwwwBackend.judge.codesandbox.model.ExcuteCodeRequest;
import com.chwww924.chwwwBackend.judge.codesandbox.model.ExecuteCodeResponse;

public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest) {
        System.out.println("Third Party Code Sandbox");
        return null;
    }
}
