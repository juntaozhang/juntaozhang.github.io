package cn.juntaozhang.design;

import java.util.PriorityQueue;

/**
 * @author juntzhang
 */
public class PriorityQueueTest {

    public static void main(String[] args) {
        PriorityQueue<Integer> q = new PriorityQueue<>((o1, o2) -> o1 - o2);
        q.offer(3);
        q.offer(1);
        q.offer(3);

        System.out.println(q.poll());
        System.out.println(q.poll());
        System.out.println(q.poll());
        System.out.println(q.poll());

    }
}
