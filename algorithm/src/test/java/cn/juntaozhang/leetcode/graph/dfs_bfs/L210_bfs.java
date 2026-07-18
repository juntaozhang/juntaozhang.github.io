package cn.juntaozhang.leetcode.graph.dfs_bfs;

import cn.juntaozhang.utils.StringUtils;
import org.junit.Test;

import java.util.*;

public class L210_bfs {

    public int[] findOrder(int numCourses, int[][] prerequisites) {
        // 1. 建邻接表：记录每个节点能指向哪些后继节点
        Map<Integer, List<Integer>> posts = new HashMap<>();

        // 2. 统计入度数组：inDegree[x] = 课程 x 有多少门前置课没上完
        int[] indegree = new int[numCourses];

        // 防止循环入队列
        boolean[] visited = new boolean[numCourses];
        for (int[] pre : prerequisites) {
            posts.compute(pre[1], (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(pre[0]);
                return v;
            });
            indegree[pre[0]]++;
        }

        // 3. 初始化队列：把所有入度 = 0 的节点入队（无前置课，可直接学）
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (indegree[i] == 0) {
                queue.add(i);
            }
        }
        List<Integer> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            int cour = queue.poll();
            result.add(cour);
            visited[cour] = true;
            List<Integer> nexts = posts.get(cour);
            if (nexts != null) {
                nexts.forEach(next -> {
                    // 前置课已经完成
                    indegree[next]--;
                    // 把所有入度 = 0 的节点入队（无前置课，可直接学）
                    if (indegree[next] == 0 && !visited[next]) {
                        queue.add(next);
                    }
                });
            }
        }
        // 4. 判环：若结果数组长度 ≠ 总课程数 → 图有环，返回[]；否则返回结果
        if (result.size() == numCourses) {
            return result.stream().mapToInt(Integer::intValue).toArray();
        } else {
            return new int[0];
        }
    }

    @Test
    public void case1() {
        StringUtils.print(findOrder(4, new int[][]{{1, 0}, {2, 0}, {3, 1}, {3, 2}}));
    }

    @Test
    public void case2() {
        StringUtils.print(findOrder(3, new int[][]{}));
    }

    @Test
    public void case3() {
        StringUtils.print(findOrder(3, new int[][]{{1, 0}, {1, 2}, {0, 1}}));
    }

}