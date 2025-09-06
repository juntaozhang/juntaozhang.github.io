package cn.juntaozhang.leetcode.回溯;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode.cn/problems/permutations-ii/
 * 回溯算法
 * 1. 交换数组元素
 * 2. 递归调用
 * 3. 回溯
 */
public class L47 {

    @Test
    public void case1() {
        int[] nums = {1, 1, 2, 2};
        List<List<Integer>> res = permuteUnique(nums);
        System.out.println(res);
    }

    @Test
    public void case2() {
        int[] nums = {1, 2, 3};
        List<List<Integer>> res = permuteUnique(nums);
        System.out.println(res);
    }

    public List<List<Integer>> permuteUnique(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums); // 排序数组
        boolean[] used = new boolean[nums.length];
        backtrack(nums, used, new ArrayList<>(), res);
        return res;
    }

    private void backtrack(int[] nums, boolean[] used, List<Integer> path, List<List<Integer>> res) {
        if (path.size() == nums.length) {
            res.add(new ArrayList<>(path));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            // 剪枝条件：
            // 1. 如果当前元素已被使用，跳过
            // 2. 如果当前元素和前一个元素相同，且前一个元素未被使用，跳过
            if (used[i] || (i > 0 && nums[i] == nums[i - 1] && !used[i - 1])) {
                continue;
            }

            used[i] = true;
            path.add(nums[i]);
            backtrack(nums, used, path, res);
            // 回溯
            used[i] = false;
            path.remove(path.size() - 1);
        }
    }
}
