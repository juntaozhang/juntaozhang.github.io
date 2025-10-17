package cn.juntaozhang.leetcode.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author juntzhang
 */
public class L802 {

    public List<Integer> eventualSafeNodes(int[][] graph) {
        List<Integer> ans = new ArrayList<>();
        int[] vist = new int[graph.length];
        while (true) {
            Integer node = findZeroOutDegree(graph, vist);
            if (node == null) {
                break;
            } else {
                ans.add(node);
            }
        }
        ans.sort((o1, o2) -> o1 - o2);
        return ans;
    }

    private Integer findZeroOutDegree(int[][] graph, int[] vist) {
        for (int i = 0; i < graph.length; i++) {
            if (graph[i].length == 0 && vist[i] == 0) {
                vist[i] = 1;
                removeChild(graph, i);
                return i;
            }
        }
        return null;
    }

    private void removeChild(int[][] graph, int x) {
        for (int i = 0; i < graph.length; i++) {
            if (graph[i].length > 0) {
                graph[i] = Arrays.stream(graph[i]).filter(c -> c != x).toArray();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new L802().eventualSafeNodes(new int[][]{
                {1, 2, 3, 4}, {1, 2}, {3, 4}, {0, 4}, {}
        }));
    }
}
