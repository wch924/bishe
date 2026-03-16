package com.chwww924.chwwwBackend.testCode.Template;

public abstract class TemplateAll {
    public final void prepare() {
        step1();
        step2();
        step3();
        step4();
    }

    protected abstract void step3();
    private void step4() {
        System.out.println("step4");
    }

    private void step2() {
        System.out.println("step2");
    }

    private void step1() {
        System.out.println("step1");
    }
}
