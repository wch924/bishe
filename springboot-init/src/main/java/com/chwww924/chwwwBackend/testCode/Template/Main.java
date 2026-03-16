package com.chwww924.chwwwBackend.testCode.Template;

public class Main {
    public static void main(String[] args)
    {
        TemplateAll aliPay = new aliPay();
        aliPay.prepare();
        TemplateAll wxPay = new wxPay();
        wxPay.prepare();
    }
}
