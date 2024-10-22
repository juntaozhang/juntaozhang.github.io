package cn.juntaozhang.leetcode.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * @author juntzhang
 */
public class L365 {
    public long hash(int[] state) {
        return (long) state[0] * 1000001 + state[1];
    }

    public boolean canMeasureWater(int jug1Capacity, int jug2Capacity, int targetCapacity) {
        Deque<int[]> stack = new ArrayDeque<>();
        Set<Long> vist = new HashSet<>();
        int[] tuple = new int[]{0, 0};
        stack.offerLast(tuple);
        vist.add(hash(tuple));

        while (!stack.isEmpty()) {
            tuple = stack.pollFirst();
            int x = tuple[0], y = tuple[1];

            if (x - targetCapacity == 0 || y - targetCapacity == 0 || x + y - targetCapacity == 0) {
                return true;
            }

            int[] next = new int[]{x, jug2Capacity};
            if (!vist.contains(hash(next))) {
                vist.add(hash(next));
                stack.offerLast(next);
            }
            next = new int[]{jug1Capacity, y};
            if (!vist.contains(hash(next))) {
                vist.add(hash(next));
                stack.offerLast(next);
            }

            next = new int[]{0, y};
            if (!vist.contains(hash(next))) {
                vist.add(hash(next));
                stack.offerLast(next);
            }
            next = new int[]{x, 0};
            if (!vist.contains(hash(next))) {
                vist.add(hash(next));
                stack.offerLast(next);
            }

            next = new int[]{x - Math.min(x, jug2Capacity - y), y + Math.min(x, jug2Capacity - y)};
            if (!vist.contains(hash(next))) {
                vist.add(hash(next));
                stack.offerLast(next);
            }
            next = new int[]{x + Math.min(jug1Capacity - x, y), y - Math.min(jug1Capacity - x, y)};
            if (!vist.contains(hash(next))) {
                vist.add(hash(next));
                stack.offerLast(next);
            }
        }
        return false;
    }

    public static void main(String[] args) {
//        System.out.println(new L365().canMeasureWater(2, 6, 5));
//        System.out.println(new L365().canMeasureWater(22003, 31237, 1));
    }
}
