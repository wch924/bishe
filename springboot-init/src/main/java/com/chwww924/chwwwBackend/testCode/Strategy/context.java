package com.chwww924.chwwwBackend.testCode.Strategy;

public class context {
    private Strategy strategy;
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
    public void run() {
        strategy.run();
    }
}
