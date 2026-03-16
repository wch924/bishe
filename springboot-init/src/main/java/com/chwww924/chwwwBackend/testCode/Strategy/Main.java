package com.chwww924.chwwwBackend.testCode.Strategy;

public class Main {
    public static void main(String[] args) {
        context context = new context();
        context.setStrategy(new Stratgey01());
        context.run();
        context.setStrategy(new Strategy2());
        context.run();
    }
}
