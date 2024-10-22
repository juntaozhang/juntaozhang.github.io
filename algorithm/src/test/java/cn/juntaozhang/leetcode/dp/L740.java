package cn.juntaozhang.leetcode.dp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author juntzhang
 */
public class L740 {

    public int deleteAndEarn(int[] nums) {

        Map<Integer, Integer> values = new HashMap<>();
        Map<Integer, Integer> dp = new HashMap<>();
        int maxNum = nums[0];
        for (int i : nums) {
            values.put(i, values.getOrDefault(i, 0) + i);
            maxNum = Math.max(maxNum, i);
        }

        dp.put(0, values.getOrDefault(0, 0));
        dp.put(1, Math.max(dp.get(0), values.getOrDefault(1, 0)));

        int ans = dp.get(1);
        for (int i = 2; i <= maxNum; i++) {
            dp.put(i, Math.max(dp.get(i - 1), dp.get(i - 2) + values.getOrDefault(i, 0)));
            ans = Math.max(ans, dp.get(i));
        }
        return ans;
    }

    public static void main(String[] args) {
//    System.out.println(new L740().deleteAndEarn(new int[]{2,2,3,3,3,4}));
//    System.out.println(new L740().deleteAndEarn(new int[]{3, 4, 2}));
        System.out.println(new L740().deleteAndEarn(new int[]{1}));
    }
}
