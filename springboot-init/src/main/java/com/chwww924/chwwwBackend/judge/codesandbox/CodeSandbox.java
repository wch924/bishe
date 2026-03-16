package com.chwww924.chwwwBackend.judge.codesandbox;
import com.chwww924.chwwwBackend.judge.codesandbox.model.ExcuteCodeRequest;
import com.chwww924.chwwwBackend.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest);

}
