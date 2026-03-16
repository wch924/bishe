package com.chwww924.chwwwBackend.testCode.Singleton;

public class singleton {
    private static volatile singleton instance;
    private singleton() {
    }
    public static singleton getInstance() {
        if (instance == null) {
            synchronized (singleton.class) {
                if (instance == null) {
                    instance = new singleton();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        // 多线程测试
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                singleton instance = singleton.getInstance();
                System.out.println(instance.hashCode()); // 所有线程输出相同哈希值
            }).start();
        }
    }
}
