package com.chwww924.chwwwBackend.judge.codesandbox;

import com.chwww924.chwwwBackend.judge.codesandbox.model.ExcuteCodeRequest;
import com.chwww924.chwwwBackend.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeSandboxProxy implements CodeSandbox{
    private final CodeSandbox codeSandbox;

    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }
    @Override

    public ExecuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest) {
        log.info("codeSandboxProxy");
        ExecuteCodeResponse request = codeSandbox.excuteCode(excuteCodeRequest);
        return request;
    }
}
