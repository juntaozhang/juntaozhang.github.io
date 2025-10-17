package cn.juntaozhang.leetcode.dp;

import java.util.*;

/**
 * @author juntzhang
 */
public class L368 {

    public List<Integer> largestDivisibleSubset(int[] nums) {
        Arrays.sort(nums);
        Map<Integer, Set<Integer>> dp = new HashMap<>();
        int max = 0, maxIdx = 0;
        for (int i = 0; i < nums.length; i++) {
            Set<Integer> tmp = new HashSet<>();
            tmp.add(nums[i]);
            dp.put(i, tmp);
            int v = 0, k = 0;
            for (int j = 0; j <= i; j++) {
                if (nums[i] % nums[j] == 0) {
                    if (v < dp.get(j).size()) {
                        v = dp.get(j).size();
                        k = j;
                    }
                }
            }
            tmp.addAll(dp.get(k));
            if (tmp.size() > max) {
                max = tmp.size();
                maxIdx = i;
            }
        }
        List<Integer> ans = new ArrayList<>(dp.get(maxIdx));
        Collections.sort(ans);
        return ans;
    }

    public List<Integer> largestDivisibleSubset2(int[] nums) {
        Arrays.sort(nums);
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        int m = 1, c = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[i] % nums[j] == 0) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            if (dp[i] > m) {
                m = dp[i];
                c = i;
            }
        }
        List<Integer> ans = new ArrayList<>();
        ans.add(nums[c]);
        m--;
        for (int i = c - 1; i >= 0; i--) {
            if (nums[c] % nums[i] == 0 && dp[i] == m) {
                ans.add(nums[i]);
                c = i;
                m--;
            }
        }
        Collections.sort(ans);
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L368().largestDivisibleSubset2(new int[]{4, 8, 10, 240}));
    }


}
