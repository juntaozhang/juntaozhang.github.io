package cn.juntaozhang.leetcode.回溯;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class L46_2 {
    @Test
    public void case1() {
        int[] nums = {1, 2, 3};
        List<List<Integer>> res = permute(nums);
        System.out.println(res);
    }

    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> res = new ArrayList<List<Integer>>();
        backtrack(nums, 0, res);
        return res;
    }

    private void backtrack(int[] nums, int start, List<List<Integer>> result) {
        // 2 3 1 |
        // 2 3 | 1
        // 2 | 1 3
        // | 1 2 3
        if (start == nums.length) {
            result.add(Arrays.stream(nums).boxed().toList());
            return;
        }
        for (int i = start; i < nums.length; i++) {
            swap(nums, start, i);
            // 2 3 1 | *

            // 2 1 | 3
            // 2 3 | 1 *

            // 1 | 2 3
            // 2 | 1 3 *
            // 3 | 2 1
            backtrack(nums, start + 1, result);
            swap(nums, start, i);
        }
    }

    private static void swap(int[] nums, int i, int j) {
        if (i == j) return;
        int t = nums[i];
        nums[i] = nums[j];
        nums[j] = t;
    }
}
