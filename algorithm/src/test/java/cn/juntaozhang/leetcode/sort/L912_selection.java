package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class L912_selection {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(new L912_selection().sortArray(new int[]{5, 2, 3, 1})));
        System.out.println(Arrays.toString(new L912_selection().sortArray(new int[]{5, 1, 1, 2, 0, 0})));
    }

    public int[] sortArray(int[] nums) {
        int n = nums.length;
        System.out.println("-1:" + Arrays.toString(nums));
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (nums[min] > nums[j]) {
                    min = j;
                }
            }
            if (i != min) {
                swap(nums, i, min);
            }
            System.out.println(i + ":" + Arrays.toString(nums));
        }
        return nums;
    }

    public void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
