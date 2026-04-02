package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

public class L232 {
    class MyQueue {
        static class Node {
            int v;
            Node next;

            public Node(int v) {
                this.v = v;
            }

            @Override
            public String toString() {
                return "Node{" +
                        "v=" + v +
                        '}';
            }
        }

        Node head, tail;
        int size;

        public MyQueue() {
            head = new Node(-1);
            tail = head;
            size = 0;
        }

        public void push(int x) {
            tail.next = new Node(x);
            tail = tail.next;
            size++;
        }

        public int pop() {
            if (empty()) {
                return -1;
            }

            int v = head.next.v;
            head.next = head.next.next;
            size--;
            if(size == 0) {
                tail = head;
            }
            return v;
        }

        public int peek() {
            if (empty()) {
                return -1;
            }
            return head.next.v;
        }

        public boolean empty() {
            return size == 0;
        }
    }

    @Test
    public void case1() {
        MyQueue myQueue = new MyQueue();
        myQueue.push(1); // queue is: [1]
        System.out.println(myQueue.pop()); // return 1, queue is [2]
        myQueue.push(2); // queue is: [1, 2] (leftmost is front of the queue)
        myQueue.peek(); // return 1
        System.out.println(myQueue.pop());
        myQueue.empty();
    }

}
