package cn.juntaozhang.leetcode.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author juntzhang
 */
public class L841 {

    Deque<Integer> stack = new ArrayDeque<>();
    boolean[] vist;

    public boolean canVisitAllRooms(List<List<Integer>> rooms) {
        vist = new boolean[rooms.size()];
        stack.offerLast(0);
        vist[0] = true;
        dfs(rooms, 0);
        boolean ans = true;
        for (boolean v : vist) {
            ans = ans && v;
        }
        return ans;
    }

    public void dfs(List<List<Integer>> rooms, int x) {
        for (Integer r : rooms.get(x)) {
            if (!vist[r]) {
                vist[r] = true;
                stack.offerLast(r);
                dfs(rooms, r);
                stack.pollLast();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(new L841().canVisitAllRooms(List.of(
                List.of(2),
                List.of(),
                List.of(1)
        )));


    }
}
