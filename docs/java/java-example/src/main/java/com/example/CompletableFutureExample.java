package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureExample {
    public static void main(String[] args) throws InterruptedException {
        testThenAccept();
        testThenAcceptAsync();
    }

    public static void testThenAccept() throws InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Future的线程：" + Thread.currentThread().getName());
                future.complete("Done!"); // 完成Future
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        }, "thread-1").start();

        // use original thread
        future.thenAccept(result -> {
            System.out.println("thenAccept回调执行线程：" + Thread.currentThread().getName());
            System.out.println("消费结果：" + result);
        });

        Thread.sleep(2000);
    }

    public static void testThenAcceptAsync() throws InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Future的线程：" + Thread.currentThread().getName());
            return "Done!";
        });

        // use another thread
        future.thenAcceptAsync(result -> {
            System.out.println("Future的回调线程：" + Thread.currentThread().getName());
            System.out.println("result：" + result);
        }, pool);

        Thread.sleep(1000);
        pool.shutdown();
    }
}
