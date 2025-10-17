package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 */
public class LargestDivisibleSubset {
    @Test
    public void largestDivisibleSubset() {
        System.out.println(largestDivisibleSubset2(new int[]{1,2,3}));
    }

    public List<Integer> largestDivisibleSubset2(int[] nums) {
        int[] pre = new int[nums.length];
        int[] dp = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            pre[i] = i;
            dp[i] = 1;
            for (int j = 0; j < i; j++) {
                if (nums[i] % nums[j] == 0 && dp[i] < dp[j] + 1) {
                    dp[i] = dp[j] + 1;
                    pre[i] = j;
                }
            }
        }

        int maxVal = 0, idx = 0;
        for (int i = 0; i < dp.length; i++) {
            if (dp[i] > maxVal) {
                maxVal = dp[i];
                idx = i;
            }
        }
        List<Integer> res = new ArrayList<>();
        res.add(nums[idx]);
        while (idx != pre[idx]) {
            idx = pre[idx];
            res.add(nums[idx]);
        }
        return res;
    }

    public List<Integer> largestDivisibleSubset(int[] nums) {
        Arrays.sort(nums);
        int[] f = new int[nums.length];
        int[] pre = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            f[i] = 1;
            pre[i] = i;
            for (int j = 0; j < i; j++) {
                if (nums[i] % nums[j] == 0 && f[i] < f[j] + 1) {
                    f[i] = f[j] + 1;
                    pre[i] = j;
                }
            }
        }
        System.out.println(Arrays.toString(pre));
        System.out.println(Arrays.toString(f));
        List<Integer> ans = new ArrayList<Integer>();
        if (nums.length == 0) {
            return ans;
        }
        int max = 0;
        int max_i = 0;
        for (int i = 0; i < nums.length; i++) {
            if (f[i] > max) {
                max = f[i];
                max_i = i;
            }
        }
        ans.add(nums[max_i]);
        while (max_i != pre[max_i]) {
            max_i = pre[max_i];
            ans.add(nums[max_i]);
        }
        Collections.reverse(ans);
        return ans;
    }

}
