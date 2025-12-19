//package com.hmdp.utils;
//
//import org.springframework.context.SmartLifecycle;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class OrderProcessor implements SmartLifecycle {
//    private volatile boolean running = false;
//    private final ExecutorService executor = Executors.newSingleThreadExecutor();
//
//    @Override
//    public void start() {
//        running = true;
//        executor.submit(this::processOrders);
//    }
//
//    @Override
//    public void stop(Runnable callback) {
//        stop();
//        callback.run();
//    }
//
//    @Override
//    public void stop() {
//        running = false;
//        executor.shutdownNow();
//        try {
//            executor.awaitTermination(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    @Override
//    public boolean isRunning() {
//        return running;
//    }
//
//    @Override
//    public int getPhase() {
//        return Integer.MAX_VALUE; // 确保最后关闭
//    }
//
//    private void processOrders() { /* 同前 */ }
//}
