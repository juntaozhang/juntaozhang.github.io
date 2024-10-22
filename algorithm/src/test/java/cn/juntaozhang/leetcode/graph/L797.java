package cn.juntaozhang.leetcode.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * @author juntzhang
 */
public class L797 {

    List<List<Integer>> ans = new ArrayList<>();
    Deque<Integer> stack = new ArrayDeque<>();

    public List<List<Integer>> allPathsSourceTarget(int[][] graph) {
        stack.offerLast(0);
//    dfs(graph, 0);
        dfs2(graph);
        stack.pollLast();
        return ans;
    }

    public void dfs(int[][] graph, int x) {
        if (x == graph.length - 1) {
            ans.add(new ArrayList<>(stack));
            return;
        }

        for (int next : graph[x]) {
            stack.offerLast(next);
            dfs(graph, next);
            stack.pollLast();
        }
    }

    // todo 怎么改写 非递归
    public void dfs2(int[][] graph) {
        while (!stack.isEmpty()) {
            int x = stack.pollLast();
            System.out.print(x + "\t");
            if (x == graph.length - 1) {
                System.out.println();
            }
            for (int next : graph[x]) {
                stack.offerLast(next);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new L797().allPathsSourceTarget(new int[][]{
                {1, 2}, {3}, {3}, {}
        }));
    }
}
