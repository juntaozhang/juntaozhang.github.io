package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;

public class CountingSort {

    public static void sort(int[] nums) {
        if (nums.length == 0) {
            return;
        }
        int n = Arrays.stream(nums).max().getAsInt() + 1;
        int[] counting = new int[n];
        for (int num : nums) {
            counting[num]++;
        }
        System.out.println(Arrays.toString(counting));

//        int preSum = 0;
//        for (int i = 1; i < n; i++) {
//            int t = counting[i];
//            counting[i] = preSum;
//            preSum += t;
//        }

        /*
        [0, 0, 0, 0, 0, 0, 0,  7, 8, 9, 10, 11]
        [0, 0, 0, 0, 0, 0, 0,  2, 1, 1, 0,  2]
        [0, 0, 0, 0, 0, 0, 0,  0, 2, 3, 4,  4]
        [0, 0, 0, 0, 0, 0, 0,  4, 3, 2, 2,  0]
         */
        int preSum = 0;
        for (int i = n - 1; i >= 0; i--) {
            int t = counting[i];
            counting[i] = preSum;
            preSum += t;
        }


        System.out.println(Arrays.toString(counting));

        int[] ans = new int[nums.length];
        for (int i = 0; i < ans.length; i++) {
            int d = nums[i];
            ans[counting[d]++] = d;
        }
        nums = ans;
        System.out.println(Arrays.toString(nums));
    }

    public static void main(String[] args) {
        /*
          0 1 2 3 4
          0 1 3 4
          6 7 7 8 9
         */
//        sort(new int[]{7, 8, 9, 7, 6});
        sort(new int[]{7, 8, 9, 7, 6, 7, 6, 8, 6, 6});
        sort(new int[]{7, 8, 9, 7, 11, 11});
    }
}
