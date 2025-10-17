package cn.juntaozhang.leetcode.greedy;

import java.util.Arrays;

public class L1029 {

    public int twoCitySchedCost(int[][] costs) {
        Arrays.sort(costs, (a, b) -> {
            return a[0] - a[1] - (b[0] - b[1]);
        });

        int n = costs.length / 2;
        int ans = 0;
        for (int i = 0; i < n; i++) {
            ans += (costs[i][0] + costs[costs.length - i - 1][1]);
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(new L1029().twoCitySchedCost(new int[][]{
                {10, 20}, {30, 200}, {400, 50}, {30, 20}
        }));
    }
}
