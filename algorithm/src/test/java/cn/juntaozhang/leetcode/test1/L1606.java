package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 你有 k 个服务器，编号为 0 到 k-1。它们可以同时处理多个请求，但每个服务器同一时间只能处理一个请求。
 * 给你一个数组 arrival 和一个数组 load，其中 arrival[i] 是第 i 个请求到达的时间，load[i] 是该请求需要处理的时间。
 * 第 i 个请求（从 0 开始计数）按照以下规则分配给服务器：
 * 如果编号为 (i % k) 的服务器是空闲的，则将该请求分配给它。
 * 否则，将该请求分配给下一个可用的服务器（即 (i + 1) % k, (i + 2) % k 等等），从 (i % k) 开始按顺序查找。
 * 如果所有服务器都忙，则该请求被丢弃，不进行任何处理。
 * 服务器处理完一个请求后会立即变为空闲状态，可以处理新的请求。如果一个请求在服务器变为空闲的同一时刻到达，该服务器可以处理这个请求。
 * <p>
 * 你的任务是找出处理请求数量最多的服务器编号。如果有多个服务器处理了相同数量的最多请求，返回所有这些服务器的编号。
 * <p>
 * <p>
 * 📝 示例 1
 * <p>
 * 输入:
 * k = 3
 * arrival = [1, 2, 3, 4, 5]
 * load = [5, 2, 3, 3, 3]
 * <p>
 * 输出:
 * [1]
 * <p>
 * 📝 示例 2
 * <p>
 * 输入:
 * k = 3
 * arrival = [1, 2, 3, 4]
 * load = [1, 2, 1, 2]
 * <p>
 * 输出:
 * [0]
 */
public class L1606 {

    public List<Integer> busiestServers(int k, int[] arrival, int[] load) {
        PriorityQueue<int[]> queue = new PriorityQueue<>((e1, e2) -> e1[0] - e2[0]);
        for (int i = 0; i < arrival.length; i++) {
            if (queue.size() < k) {
                queue.offer(new int[]{arrival[i] + load[i], i, 1});
                continue;
            }
            if (i >= queue.peek()[0]) {
                int[] server = queue.poll();
                server[2]++;
                server[0] = arrival[i] + load[i];
                queue.offer(server);
            }
        }
        PriorityQueue<int[]> queue2 = new PriorityQueue<>((e1, e2) -> e2[2] - e1[2]);
        while (!queue.isEmpty()) {
            queue2.offer(queue.poll());
        }

        int cnt = queue2.peek()[2];
        List<Integer> ans = new ArrayList<>();
        while (!queue2.isEmpty()) {
            int[] server = queue2.poll();
            if (server[2] == cnt) {
                ans.add(server[1]);
            } else {
                break;
            }
        }
        return ans;
    }

    @Test
    public void case1() {
        List<Integer> res = busiestServers(3, new int[]{1, 2, 3, 4, 5}, new int[]{5, 2, 3, 3, 3});
        System.out.println(res);
    }

    @Test
    public void case2() {
        List<Integer> res = busiestServers(3, new int[]{1, 2, 3, 4}, new int[]{1, 2, 1, 2});
        System.out.println(res);
    }
}
