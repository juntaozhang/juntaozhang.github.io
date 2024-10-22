package cn.juntaozhang.leetcode;

/**
 * 
 */
public class L877_stone_game {
    class Solution {
        int n = 0;
        int[] piles;
        int[][] cache;

        public boolean stoneGame(int[] piles) {
            n = piles.length;
            cache = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    cache[i][j] = Integer.MIN_VALUE;
                }
            }
            this.piles = piles;
            int diff1 = piles[0] - solve(1, n - 1);
            int diff2 = piles[n - 1] - solve(0, n - 2);
            return diff1 > diff2;
        }

        public int solve(int i, int j) {
            if (i == j) {
                return piles[i];
            }
            if (cache[i][j] != Integer.MIN_VALUE) {
                return cache[i][j];
            }
            int diff = Math.max(piles[i] - solve(i + 1, j), piles[j] - solve(i, j - 1));
            System.out.println(i + "," + j + "=" + diff);
            cache[i][j] = diff;
            return diff;
        }
    }

    public static void main(String[] args) {
        Solution s = new L877_stone_game().new Solution();
        System.out.println(s.stoneGame(new int[]{5, 3, 4, 6}));
    }
}
