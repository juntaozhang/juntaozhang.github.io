package cn.juntaozhang.leetcode;

import org.junit.Test;

/**
 * 给你单链表的头节点 head ，请你反转链表，并返回反转后的链表。
 * <p>
 * <p>
 * 示例 1：
 * <p>
 * <p>
 * 输入：head = [1,2,3,4,5]
 * 输出：[5,4,3,2,1]
 * 示例 2：
 * <p>
 * <p>
 * 输入：head = [1,2]
 * 输出：[2,1]
 * 示例 3：
 * <p>
 * 输入：head = []
 * 输出：[]
 *
 * 
 */
public class L206 {
    @Test
    public void case1() {
        ListNode l1 = new ListNode(1);
        ListNode l2 = new ListNode(2);
        ListNode l3 = new ListNode(3);
        l1.next = l2;
        l2.next = l3;
        reverseList(l3);
    }

    public ListNode reverseList(ListNode head) {
        if(head == null || head.next == null) {
            return head;
        }
        // new list point
        ListNode p1 = head;

        // old list point
        ListNode p2 = head.next, t;
        p1.next = null;

        while(p2 != null) {
            t = p2.next;
            p2.next = p1;
            p1 = p2;
            p2 = t;
        }

        return p1;
    }

    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }

        @Override
        public String toString() {
            return val + "";
        }
    }
}
