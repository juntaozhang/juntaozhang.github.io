package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class L912_shell {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(new L912_shell().sortArray(new int[]{1})));
        System.out.println(Arrays.toString(new L912_shell().sortArray(new int[]{5, 2, 3, 1})));
        System.out.println(Arrays.toString(new L912_shell().sortArray(new int[]{5, 1, 1, 2, 0, 0, 2, 4})));
    }

    public int[] sortArray(int[] nums) {
        int n = nums.length;
        System.out.println(" - :" + Arrays.toString(nums));
        for (int gap = n / 2; gap > 0; gap /= 2)
            for (int k = 0; k < gap; k++) {
                for (int i = k + gap; i < n; i += gap) {
                    // current 站起来
                    int current = nums[i];
                    // 找出 坐下的位置
                    int j = i - gap;
                    while (j >= k && current < nums[j]) {
                        // current:5
                        // 4 6 j后移
                        nums[j + gap] = nums[j];
                        j -= gap;
                    }
                    // current 坐下, j -> 4
                    nums[j + gap] = current;
                }
                System.out.println(gap + "-" + k + ":" + Arrays.toString(nums));
            }
        return nums;
    }

    public int[] sortArray2(int[] nums) {
        int n = nums.length;
        for (int gap = n / 2; gap > 0; gap /= 2)
            for (int k = 0; k < gap; k++) {
                for (int i = k; i < n; i += gap) {
                    for (int j = i; j >= gap; j -= gap) { // 每个都要遍历一遍
                        if (nums[j - gap] > nums[j]) {
                            swap(nums, j, j - gap);
                        }
                    }
                }
            }
        return nums;
    }

    public void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}