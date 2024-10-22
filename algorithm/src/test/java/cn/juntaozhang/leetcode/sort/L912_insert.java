package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class L912_insert {
    public static void main(String[] args) {
//        System.out.println(Arrays.toString(new L912_insert().sortArray(new int[]{5, 2, 3, 1})));
        System.out.println(Arrays.toString(new L912_insert().sortArray(new int[]{5, 1, 1, 2, 0, 0})));
    }

    public int[] sortArray(int[] nums) {
        int n = nums.length;
        System.out.println("-:" + Arrays.toString(nums));
        for (int i = 1; i < n; i++) {
            for (int j = i; j > 0; j--) {
                if (nums[j - 1] > nums[j]) {
                    swap(nums, j, j - 1);
                }
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
