package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.LinkedList;

/**
 * 
 */
public class SlidingWindowAverageFromDataStream {
    //======================================Sliding Window Average from Data Stream=====================================
    public class MovingAverage {
        private int[] cache;
        private int p = 0;
        private int count = 0;
        private int size;

        /**
         * Initialize your data structure here.
         */
        public MovingAverage(int size) {
            this.size = size;
            cache = new int[size];
        }

        public double next(int val) {
            cache[p % size] = val;
            p++;
            count++;
            double t = 0.0;
            for (int i = 0; i < Math.min(size, count); i++) {
                t += cache[i];
            }
            return t / Math.min(size, count);
        }
    }

    @Test
    public void movingAverage() {
        MovingAverage obj = new MovingAverage(3);
        System.out.println(obj.next(1));
        System.out.println(obj.next(10));
        System.out.println(obj.next(3));
        System.out.println(obj.next(5));
    }

    public class MovingAverage2 {
        private double[] buff;
        private int p = 0;//当前指针
        private int size;//数组大小

        /**
         * Initialize your data structure here.
         */
        public MovingAverage2(int size) {
            this.size = size;
            buff = new double[1000000];
        }

        public double next(int val) {
            p++;
            buff[p] = buff[p - 1] + val;
            if (p < size) {
                return buff[p] / (p);
            }
            return (buff[p] - buff[p - size]) / (size);
        }
    }

    @Test
    public void movingAverage2() {
        MovingAverage2 obj = new MovingAverage2(3);
        System.out.println(obj.next(1));
        System.out.println(obj.next(10));
        System.out.println(obj.next(3));
        System.out.println(obj.next(5));
    }

    public class MovingAverage3 {
        private double[] buff;
        private int p = 0;//当前指针
        private int size;//数组大小

        /**
         * Initialize your data structure here.
         */
        public MovingAverage3(int size) {
            this.size = size;
            buff = new double[size + 1];
        }

        private int mod(int i) {
            return i % (size + 1);
        }

        public double next(int val) {
            p++;
            buff[mod(p)] = buff[mod(p - 1)] + val;
            if (p < size) {
                return buff[p] / p;
            }
            return (buff[mod(p)] - buff[mod(p - size)]) / size;
        }
    }


    public class MovingAverage4 {
        private int size;
        private LinkedList<Integer> q;
        private double sum = 0;

        /**
         * Initialize your data structure here.
         */
        public MovingAverage4(int size) {
            this.size = size;
            this.q = new LinkedList<Integer>();
        }

        public double next(int val) {
            sum += val;
            q.offer(val);
            if (q.size() <= size) {
                return sum / q.size();
            }
            int h = q.poll();
            sum -= h;
            return sum / size;
        }
    }

    @Test
    public void movingAverage4() {
        MovingAverage4 obj = new MovingAverage4(3);
        System.out.println(obj.next(1));
        System.out.println(obj.next(10));
        System.out.println(obj.next(3));
        System.out.println(obj.next(5));
    }

}
