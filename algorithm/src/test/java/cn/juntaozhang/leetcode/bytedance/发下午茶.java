package cn.juntaozhang.leetcode.bytedance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Scanner;

public class 发下午茶 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int k = scanner.nextInt();
        int n = scanner.nextInt();
        int[] t = new int[n];
        for (int i = 0; i < n; i++) {
            t[i] = scanner.nextInt();
        }
        System.out.println(new 发下午茶().findMinCost(k, n, t));
    }

    /**
     * 输入：
     * 3 3
     * 7 1 1
     * 输出：5
     * 解释：
     * 字节君1：右移->放置->放置->放置->放置
     * 字节君2：右移->放置->放置->放置
     * 字节君3：右移->右移->放置->右移->放置
     *
     * @param k 字节君的数量K
     * @param n 工区的数量 N
     * @param t N 个数字是每个工区需要的下午茶数量 Ti
     * @return 输出一个数字代表所有字节均最少花费多长时间才能送完所有的下午茶
     */
    public int findMinCost(int k, int n, int[] t) {
        // 二分查找
        int max = Arrays.stream(t).sum() + n;
        int min = n;
        while (min < max) {
            int mid = (max - min) / 2 + min;
            if (check(k, mid, t)) {
                max = mid;
            } else {
                min = mid + 1;
            }
        }
        return max;
    }

    public boolean check(int k, int time, int[] tea) {
        // 3
        // 7
        // 1, 1, 7
        int[] t = Arrays.copyOf(tea, tea.length);
        for (int i = 0; i < k; i++) {
            int iTime = time;
            for (int j = 0; j < t.length; j++) {
                iTime--;
                if (t[j] == 0) {
                    continue;
                }
                if (iTime >= t[j]) {
                    iTime -= t[j];
                    t[j] = 0;
                } else {
                    t[j] -= iTime;
                    break;
                }
            }
        }
//        System.out.println(time + " " + Arrays.toString(t));
        return t[t.length - 1] == 0;
    }

    @Test
    public void case1() {
        System.out.println(findMinCost(3, 3, new int[]{7, 1, 1}));
    }

    @Test
    public void case2() {
        System.out.println(findMinCost(3, 3, new int[]{1, 1, 7}));
    }
}
