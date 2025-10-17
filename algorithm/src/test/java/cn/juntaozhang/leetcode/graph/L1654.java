package cn.juntaozhang.leetcode.graph;

import java.util.*;

/**
 * @author juntzhang
 */
public class L1654 {

    public int minimumJumps(int[] forbidden, int a, int b, int x) {
        Set<Integer> vistL = new HashSet<>();
        Set<Integer> vistR = new HashSet<>();
        for (int i : forbidden) {
            vistL.add(i);
            vistR.add(i);
        }

        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{0, 0, 1});// 0:left 1:right
        vistR.add(0);
        while (!q.isEmpty()) {
            int[] t = q.poll();
            int node = t[0], len = t[1], direction = t[2];
            if (node - x == 0) {
                return len;
            }
            int right = node + a, left = node - b;
            if (right <= 6000 && !vistR.contains(right)) {
                q.offer(new int[]{right, len + 1, 1});
                vistR.add(right);
            }
            if (left >= 0 && !vistL.contains(left) && direction != 0) {
                q.offer(new int[]{left, len + 1, 0});
                vistL.add(left);
            }
        }
        Arrays.hashCode(new int[]{1, 2});
        return -1;
    }

    public static void main(String[] args) {
        System.out.println(new L1654().minimumJumps(new int[]{1998}, 1999, 2000, 2000));
//        System.out.println(new L1654().minimumJumps(new int[]{1, 6, 2, 14, 5, 17, 4}, 16, 9, 7));
//        System.out.println(new L1654().minimumJumps(
//                new int[]{162, 118, 178, 152, 167, 100, 40, 74, 199, 186, 26, 73, 200, 127, 30, 124, 193, 84, 184, 36,
//                        103, 149, 153, 9, 54, 154, 133, 95, 45, 198, 79, 157, 64, 122, 59, 71, 48, 177, 82, 35, 14, 176,
//                        16, 108, 111, 6, 168, 31, 134, 164, 136, 72, 98},
//                29, 98, 80));
    }
}
