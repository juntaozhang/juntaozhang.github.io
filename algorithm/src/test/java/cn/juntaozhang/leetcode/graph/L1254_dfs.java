package cn.juntaozhang.leetcode.graph;

import java.util.Stack;

import static cn.juntaozhang.utils.StringUtils.print;

/**
 * @author juntzhang
 */
class L1254_dfs {

    int[][] directions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    int n = 0;
    int m = 0;

    public int closedIsland(int[][] grid) {
        n = grid.length;
        m = grid[0].length;
        int ans = 0;
        print(grid);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (dfs(grid, i, j, ans + 2)) {
                    print(grid);
                    ans++;
                }
            }
        }
        return ans;
    }

    boolean dfs(int[][] grid, int sr, int sc, int target) {
        if (grid[sr][sc] != 0) {
            return false;
        }
        Stack<int[]> stack = new Stack<>();

        grid[sr][sc] = target;
        boolean flag = true;
        stack.push(new int[]{sr, sc});

        while (!stack.isEmpty()) {
            int[] t = stack.pop();
            int tx = t[0], ty = t[1];
            if (tx == 0 || ty == 0 || tx == n - 1 || ty == m - 1) {
                flag = false;
            }
            for (int[] dir : directions) {
                int x = tx + dir[0], y = ty + dir[1];
                if (x >= 0 && x < n && y >= 0 && y < m && grid[x][y] == 0) {
                    stack.push(new int[]{x, y});
                    grid[x][y] = target;
                }
            }
        }
        return flag;
    }

    public static void main(String[] args) {
        System.out.println(new L1254_dfs().closedIsland(new int[][]{
                {1, 1, 1, 1, 1, 1, 1, 0},
                {1, 0, 0, 0, 0, 1, 1, 0},
                {1, 0, 1, 0, 1, 1, 1, 0},
                {1, 0, 0, 0, 0, 1, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 0}
        }));
    }
}
