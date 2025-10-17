package cn.juntaozhang.jdk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * 幽灵引用示例
 */
public class PhantomReferenceExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 创建一个引用队列
        ReferenceQueue<FileWriter> referenceQueue = new ReferenceQueue<>();

        // 创建一个文件和FileWriter对象
        File file = new File("example.txt");
        FileWriter writer = new FileWriter(file);

        // 创建一个幽灵引用，指向FileWriter对象，并将其与引用队列关联
        PhantomReference<FileWriter> phantomRef = new PhantomReference<>(writer, referenceQueue);

        // 启动一个线程来监控引用队列
        new Thread(() -> {
            try {
                check(referenceQueue, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 模拟写入日志
        writer.write("This is a log message.");
        writer.flush();

        // 清除强引用
        writer = null;

        Thread.sleep(2000);

        // 强制垃圾回收
        System.gc();

        // 主线程等待
        Thread.sleep(2000);
        System.out.println("Main completed");
    }

    private static void check(ReferenceQueue<FileWriter> referenceQueue, File file) throws InterruptedException {
        System.out.println("Checking for file deletion");
        // 这里会lock waiting for the reference to be enqueued
        if (referenceQueue.remove() != null) {
            System.out.println("FileWriter对象已被垃圾回收器标记为可回收");
            System.out.println("File exists:" + file.exists());
            // 在这里执行清理操作，例如删除文件
            if (file.delete()) {
                System.out.println("文件已删除");
            } else {
                System.out.println("文件删除失败");
            }
        } else {
            System.out.println("FileWriter对象尚未被垃圾回收器标记为可回收");
        }
        System.out.println("check finished.");
    }
}