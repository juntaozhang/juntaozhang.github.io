package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.TreeSet;

public class L1675 {
    public int minimumDeviation(int[] nums) {
        TreeSet<Integer> set = new TreeSet<>();
        int result = 0;
        for (Integer num : nums) {
            if (num % 2 == 1) {
                num *= 2;
            }
            set.add(num);
            result = Math.max(result, num);
        }
        while (set.first() <= set.last() && set.last() % 2 == 0) {
            int r = set.last();
            set.remove(r);
            r /= 2;
            set.add(r);
            result = Math.min(result, set.last() - set.first());
        }
        return result;
    }

    @Test
    public void case1() {
        System.out.println(minimumDeviation(new int[]{1, 2, 3, 4}));
    }

    @Test
    public void case2() {
        System.out.println(minimumDeviation(new int[]{3, 5}));
    }

    @Test
    public void case3() {
        System.out.println(minimumDeviation(new int[]{10, 4, 3}));
    }

    @Test
    public void case4() {
        System.out.println(minimumDeviation(new int[]{3, 4, 4}));
    }

    @Test
    public void case5() {
        System.out.println(minimumDeviation(new int[]{8, 12}));
    }
}
