package cn.juntaozhang.leetcode.dp;

import cn.juntaozhang.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juntzhang
 */
public class L1027 {

    public int longestArithSeqLength(int[] nums) {
        int n = nums.length;
        List<Map<Integer, Integer>> dp = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            dp.add(new HashMap<>());
        }
        int ans = 2;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                int d = nums[i] - nums[j];
                int v = dp.get(j).getOrDefault(d, 1) + 1;
                dp.get(i).compute(d, (_k, _v) -> _v == null ? v : Math.max(_v, v));
                ans = Math.max(ans, dp.get(i).get(d));
            }
        }
        return ans;
    }

    public int longestArithSeqLength2(int[] arr) {
        int n = arr.length;
        Map<Integer, List<Integer>> valueMap = new HashMap<>();
        int[][] dp = new int[n][n];
        for (int i = 0; i < n; i++) {
            int finalI = i;
            valueMap.compute(arr[i], (_k, _v) -> {
                if (_v == null) {
                    _v = new ArrayList<>();
                }
                _v.add(finalI);
                return _v;
            });
        }
        int ans = 0;
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
            for (int j = i + 1; j < n; j++) {
                int v = arr[i] - (arr[j] - arr[i]);
                List<Integer> idxList = valueMap.get(v);
                if (idxList != null) {
                    for (Integer idx : idxList) {
                        if (idx >= i) {
                            dp[i][j] = Math.max(i == j ? 1 : 2, dp[i][j]);
                        } else {
                            dp[i][j] = Math.max(dp[idx][i] + 1, dp[i][j]);
                        }
                    }
                } else {
                    dp[i][j] = Math.max(i == j ? 1 : 2, dp[i][j]);
                }
                ans = Math.max(dp[i][j], ans);
            }
        }
        StringUtils.print(dp);
        return ans;
    }

    public static void main(String[] args) {
        //                                                             0 1 2  3  4  5  6  7  8  9  10
//    System.out.println(new L1027().longestArithSeqLength(new int[]{3,6,9,4,5,6,12}));
//    System.out.println(new L1027().longestArithSeqLength(new int[]{24, 13, 1, 100, 0, 94, 3, 0, 3}));
//    System.out.println(new L1027().longestArithSeqLength(new int[]{83, 20, 17, 43, 52, 78, 68, 45}));
//    System.out.println(new L1027().longestArithSeqLength(new int[]{0,8,45,88,48,68,28,55,17,24}));
//    System.out.println(new L1027().longestArithSeqLength(new int[]{12,55,17,18,25,19,28,45,56,29,39,52,8,1,21,17,21,23,70,51,61,21,52,25,28}));
    }
}
