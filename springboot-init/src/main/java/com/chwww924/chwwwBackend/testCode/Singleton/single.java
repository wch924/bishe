package com.chwww924.chwwwBackend.testCode.Singleton;

public class single {
    private single() {

    }
    private static class Holder {
        private static final single INSTANCE = new single();
    }
    public static single getInstance() {
        return Holder.INSTANCE;
    }
    public static void main(String[] args) {
        for(int i = 0;i < 10;i++) {
            new Thread(
                    () -> {
                        single instance = single.getInstance();
                        System.out.println(instance.hashCode());
                    }
            ).start();
        }
    }
}
