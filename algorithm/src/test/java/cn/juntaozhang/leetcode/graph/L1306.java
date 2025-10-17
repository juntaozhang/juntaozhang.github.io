package cn.juntaozhang.leetcode.graph;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author juntzhang
 */
public class L1306 {

    public boolean canReach(int[] arr, int start) {
        int n = arr.length;
        boolean[] vist = new boolean[n];
        Queue<Integer> q = new LinkedList<>();
        q.offer(start);
        vist[start] = true;
        while (!q.isEmpty()) {
            int node = q.poll();
            if (arr[node] == 0) {
                return true;
            }
            if (node - arr[node] >= 0 && !vist[node - arr[node]]) {
                q.offer(node - arr[node]);
                vist[node - arr[node]] = true;
            }
            if (node + arr[node] < n && !vist[node + arr[node]]) {
                q.offer(node + arr[node]);
                vist[node + arr[node]] = true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(new L1306().canReach(new int[]{4, 2, 3, 0, 3, 1, 2}, 5));
    }
}
