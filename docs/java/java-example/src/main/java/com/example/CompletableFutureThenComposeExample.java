package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureThenComposeExample {

    public static class JobMasterGateway {
        public CompletableFuture<String> submitJob(ExecutorService pool) {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("线程【" + Thread.currentThread().getName() + "】执行：提交作业成功");
                return "job_123456";
            }, pool);
        }

        public CompletableFuture<String> getJobStatus(ExecutorService pool, String jobId) {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("线程【" + Thread.currentThread().getName() + "】执行：查询作业状态");
                return "RUNNING";
            }, pool);
        }
    }

    public static void main(String[] args) throws Exception {
        testNest();
        System.out.println("========================================");
        testFlat();
    }

    public static void testNest() throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        final JobMasterGateway gateway = new JobMasterGateway();
        CompletableFuture<String> jobIdFuture = gateway.submitJob(pool);

        CompletableFuture<CompletableFuture<String>> jobStatusFuture = jobIdFuture.thenApplyAsync(jobId -> {
            System.out.println("线程：" + Thread.currentThread().getName() + " → 拿到JobID：" + jobId + "，开始执行异步任务2");
            return gateway.getJobStatus(pool, jobId);
        }, pool);

        // ========== 重点：嵌套Future的【坑】- 获取结果的两种方式，都很恶心 ==========
        System.out.println("===== 嵌套Future获取结果 =====");
        // 方式1：双层get() 阻塞获取最终结果（最常用，但是极其臃肿）
//        String finalStatus = jobStatusFuture.get().get();
//        System.out.println("最终获取到的作业状态：" + finalStatus);

        // 方式2：双层thenAccept 异步消费结果（回调套回调 → 回调地狱）
        jobStatusFuture.thenAccept(innerFuture -> {
            innerFuture.thenAccept(status -> {
                System.out.println("线程：" + Thread.currentThread().getName() + " → 最终结果：作业状态 = " + status);
            });
        });

        Thread.sleep(1000);
        pool.shutdown();
    }


    public static void testFlat() throws InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();
        final JobMasterGateway gateway = new JobMasterGateway();

        // ======== 模拟：异步任务1 提交作业 → 返回JobID ========
        CompletableFuture<String> jobIdFuture = gateway.submitJob(pool);

        // lambda返回的是【异步任务2】，用thenComposeAsync，返回单层Future
        CompletableFuture<String> jobStatusFuture = jobIdFuture.thenComposeAsync(jobId -> {
            System.out.println("线程：" + Thread.currentThread().getName() + " → 拿到JobID：" + jobId + "，开始执行异步任务2");
            return gateway.getJobStatus(pool, jobId);
        }, pool);

        jobStatusFuture.thenAccept(status -> {
            System.out.println("线程：" + Thread.currentThread().getName() + " → 最终结果：作业状态 = " + status);
        });

        Thread.sleep(1000);
        pool.shutdown();
    }
}
