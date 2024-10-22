package cn.juntaozhang.leetcode.dp;

import cn.juntaozhang.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author juntzhang
 */
public class L873 {

    public int lenLongestFibSubseq(int[] arr) {
        // 1,2,3,4,5,6,7,8
        // dp[i_idx][j_idx]  -> arr[i], arr[j]
        /*
            dp[1][8] = 2
            dp[5][8] = dp[3][5]
            dp[6][8] = dp[2][6]
            dp[7][8] = dp[1][7]
            max(dp[i][8]) <= 2 则return 0
            dp[i][j] = dp[j - i][i] + 1, if(dp[j - i] exist)
            dp[1][1] = dp[0][1] + 1, if(dp[0][1] not exist) dp[0][1] = 0
            dp[1][2] = dp[1][1] + 1 = 2
        */
        int n = arr.length;
        Map<Integer, Integer> valueMap = new HashMap<>();
        int[][] dp = new int[n][n];
        for (int i = 0; i < n; i++) {
            valueMap.put(arr[i], i);
        }
        int ans = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int v = arr[j] - arr[i];
                Integer idx = valueMap.get(v);
                if (idx == null || idx >= i) {
                    dp[i][j] = i == j ? 1 : 2;
                } else {
                    dp[i][j] = dp[idx][i] + 1;
                }
                ans = Math.max(dp[i][j], ans);
            }
        }
        StringUtils.print(dp);
        return ans > 2 ? ans : 0;
    }

    public static void main(String[] args) {
        System.out.println(new L873().lenLongestFibSubseq(new int[]{4, 14, 18, 32, 50}));
        //                                              dp[1][1] dp[1][6]  dp[6][8]   dp[8][10]  dp[10][11]
        //                                              dp[4][4] dp[4][14] dp[14][18] dp[18][32] dp[32][50]
        //                                                          0 1 2 3 4  5  6  7  8  9 10 11
        System.out.println(new L873().lenLongestFibSubseq(new int[]{2, 4, 7, 8, 9, 10, 14, 15, 18, 23, 32, 50}));
    }
}
