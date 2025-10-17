package cn.juntaozhang.jdk;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * 通过创建一个基类 `Base` 和一个派生类 `Derived`，
 * 并在派生类中实现一个虚函数 `virtualMethod` 和一个直接方法 `directMethod`。
 * 然后，它在主方法中分别调用这两个方法多次，并测量每种方法的执行时间，最后输出性能差异。
 */
public class VirtualFunctionPerformanceTest {
    static abstract class Base {
        abstract void virtualMethod();
    }

    static class Derived extends Base {
        @Override
        void virtualMethod() {
            // Simulate some work
            int sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += i;
            }
        }

        void directMethod() {
            // Simulate some work
            int sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += i;
            }
        }
    }

    public static void main(String[] args) {
        long batch = 1_000_000;
        // Measure virtual method performance
        long startTime = System.nanoTime();
        for (int i = 0; i < batch; i++) {
            Derived derived = new Derived();
            derived.virtualMethod();
        }
        long endTime = System.nanoTime();
        long virtualMethodDuration = endTime - startTime;

        // Measure direct method performance
        startTime = System.nanoTime();
        for (int i = 0; i < batch; i++) {
            Derived derived = new Derived();
            derived.directMethod();
        }
        endTime = System.nanoTime();
        long directMethodDuration = endTime - startTime;

        NumberFormat usFormat = NumberFormat.getInstance(Locale.US);
        // Print the results
        System.out.println("Virtual method duration: " + usFormat.format(virtualMethodDuration) + " ns");
        System.out.println("Direct method duration: " + usFormat.format(directMethodDuration) + " ns");
        System.out.println("Performance overhead: " + usFormat.format(virtualMethodDuration - directMethodDuration) + " ns");
    }
}
