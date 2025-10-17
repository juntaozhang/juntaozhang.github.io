package cn.juntaozhang.leetcode.graph;

import cn.juntaozhang.utils.StringUtils;

/**
 * @author juntzhang
 */
class L210 {

    public int[] findOrder(int numCourses, int[][] prerequisites) {
        int[] ans = new int[numCourses];
        boolean[] vist = new boolean[numCourses];
        boolean[][] graph = new boolean[numCourses][numCourses];
        for (int[] p : prerequisites) {
            graph[p[0]][p[1]] = true;
        }

        for (int i = 0; i < graph.length; i++) {
            int node = getNextCourse(graph, vist);
            vist[node] = true;
            for (int j = 0; j < graph.length; j++) {
                graph[node][j] = false;
            }
            ans[i] = node;
        }

        return ans;
    }

    private int getNextCourse(boolean[][] graph, boolean[] vist) {
        for (int j = 0; j < graph.length; j++) {
            if (vist[j]) {
                continue;
            }
            boolean indegree = false;
            for (int i = 0; i < graph.length; i++) {
                if (graph[i][j]) {
                    indegree = true;
                    break;
                }
            }
            if (!indegree) {
                return j;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int[][] arrs = StringUtils.str2arr(args[1]);
        System.out.println(
                new L210().findOrder(
                        Integer.parseInt(args[0]),
                        arrs
                )
        );

//        System.out.println(new L787().findCheapestPrice(5, new int[][]{
//                {1, 2, 10}, {2, 0, 7}, {1, 3, 8}, {4, 0, 10}, {3, 4, 2}, {4, 2, 10}, {0, 3, 3}, {3, 1, 6}, {2, 4, 5}
//        }, 0, 4, 1));

//        System.out.println(new L787().findCheapestPrice(3, new int[][]{
//                {0, 1, 2}, {1, 2, 1}, {2, 0, 10}
//        }, 1, 2, 1));
//        System.out.println(new L787().findCheapestPrice(4, new int[][]{
//                {0, 1, 1}, {0, 2, 5}, {1, 2, 1}, {2, 3, 1}
//        }, 0, 3, 1));
//        System.out.println(new L787().findCheapestPrice(3, new int[][]{
//                {0, 1, 100}, {1, 2, 100}, {0, 2, 500}
//        }, 0, 2, 1));
//        System.out.println(new L787().findCheapestPrice(3, new int[][]{
//                {0, 1, 100}, {1, 2, 100}, {0, 2, 500}
//        }, 0, 2, 0));
//        System.out.println(new L787().findCheapestPrice(4, new int[][]{
//                {0, 1, 100}, {1, 2, 100}, {2, 0, 100}, {1, 3, 600}, {2, 3, 200}
//        }, 0, 3, 1));
    }
}