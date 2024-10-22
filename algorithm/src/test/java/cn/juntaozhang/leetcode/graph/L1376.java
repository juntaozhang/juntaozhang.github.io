package cn.juntaozhang.leetcode.graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author juntzhang
 */
public class L1376 {

    HashMap<Integer, ArrayList<Integer>> tree = new HashMap<>();

    public int numOfMinutes(int n, int headID, int[] manager, int[] informTime) {
        for (int i = 0; i < n; i++) {
            int p = manager[i];
            if (p == -1) {
                continue;
            }
            int finalI = i;
            tree.compute(p, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(finalI);
                return v;
            });
        }

        return dfs(headID, informTime);
    }

    public int dfs(int p, int[] informTime) {
        if (tree.get(p) == null) {
            return 0;
        }
        int v = 0;
        for (Integer c : tree.get(p)) {
            int t = dfs(c, informTime);
            v = Math.max(v, t);
        }
        return v + informTime[p];
    }

    public static void main(String[] args) {
        System.out.println(
                new L1376().numOfMinutes(6, 2, new int[]{2, 2, -1, 1, 2, 4}, new int[]{0, 1, 1, 0, 2, 0})
        );
    }
}
