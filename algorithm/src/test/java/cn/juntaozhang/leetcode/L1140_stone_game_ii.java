package cn.juntaozhang.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * https://www.youtube.com/watch?v=e_FrC5xavwI
 * cache[s][M] = max{sum(piles[s:s+x]) – solve(s+x, max(x, M)}, 1 <= x <= 2*M, s + x <= n
 *
 * 
 */
public class L1140_stone_game_ii {
    class Solution {
        int n = 0;
        int[] piles;
        Map<String, Integer> cache = new HashMap<>();

        public int stoneGameII(int[] piles) {
            n = piles.length;
            this.piles = piles;
            int total = sum(0, n);
            int diff = solve(0, 1);
            return (total + diff) / 2;
        }

        public int solve(int s, int M) {
            if (s >= n) {
                return 0;
            }
            if (cache.containsKey(s + "_" + M)) {
                return cache.get(s + "_" + M);
            }
            // s ~ s + 2M
            // 找 x 1 <= x <= 2*M && s + x <= n
            // sum(piles[s:s+x])
            // M' = max(x,M)
            // 相对分数 solve(s+x+1,M')
            int diff = Integer.MIN_VALUE;
            for (int x = 1; x >= 1 && x <= 2 * M && (s + x) <= n; x++) {
                diff = Math.max(diff, sum(s, s + x) - solve(s + x, Math.max(x, M)));
            }
            cache.put(s + "_" + M, diff);
            return diff;
        }

        public int sum(int i, int j) {
            int s = 0;
            for (; i < j; i++) {
                s += (piles[i]);
            }
            return s;
        }
    }

    public static void main(String[] args) {
        Solution s = new L1140_stone_game_ii().new Solution();
        System.out.println(s.stoneGameII(new int[]{2, 7, 9, 4, 4}));
    }
}
