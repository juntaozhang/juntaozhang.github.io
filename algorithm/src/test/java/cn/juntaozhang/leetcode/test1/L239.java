package cn.juntaozhang.leetcode.test1;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

import java.util.PriorityQueue;

public class L239 {
    public int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        PriorityQueue<int[]> queue = new PriorityQueue<>((e1,e2) -> e2[0] - e1[0]);
        int[] res = new int[n - (k - 1)];
        for (int i = 0; i < n; i++) {
            while (!queue.isEmpty() && queue.peek()[1] <= i - k) queue.poll();
            while (!queue.isEmpty() && queue.peek()[0] < nums[i]) queue.poll();
            queue.offer(new int[]{nums[i], i});
            if (i < k - 1) {
                continue;
            }
            res[i - (k - 1)] = queue.peek()[0];
        }
        return res;
    }

    @Test
    public void case1() {
        int[] res = maxSlidingWindow(new int[]{1, 3, -1, -3, 5, 7, 6, 1, 2}, 3);
        StringUtils.print(res);
    }
}
