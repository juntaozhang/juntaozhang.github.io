package cn.juntaozhang.leetcode.sort;

import java.util.Arrays;
import java.util.HashMap;

public class L506 {
    public static void main(String[] args) {
//        System.out.println(Arrays.toString(new L506().findRelativeRanks(new int[]{5, 4, 3, 2, 1})));
        System.out.println(Arrays.toString(new L506().findRelativeRanks(new int[]{10, 3, 8, 9, 4})));
    }

    public String[] findRelativeRanks(int[] score) {
        int n = score.length;
        String[] res = new String[n];
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            map.put(score[i], i);
        }
        shellSort(score);
        for (int i = 0; i < n; i++) {
            int j = map.get(score[i]);
            if (i == 0) {
                res[j] = "Gold Medal";
            } else if (i == 1) {
                res[j] = "Silver Medal";
            } else if (i == 2) {
                res[j] = "Bronze Medal";
            } else {
                res[j] = String.valueOf(i + 1);
            }
        }
        return res;
    }


    public void shellSort(int[] nums) {
        int n = nums.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int s = 0; s < gap; s++) {
                for (int i = s + gap; i < n; i += gap) {
                    int c = nums[i];
                    int j = i - gap;
                    while (j >= s && nums[j] <= c) {
                        nums[j + gap] = nums[j];
                        j -= gap;
                    }
                    nums[j + gap] = c;
                }
            }
        }
    }
}
