package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

/**
 * Definition for singly-linked list.
 */
public class L92 {
    public static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public ListNode reverseBetween(ListNode head, int left, int right) {
        if (head == null || head.next == null) {
            return head;
        }
        boolean isStart = left == 1;
        ListNode l = head, r;
        int i = 1;
        while (i < left - 1) {
            l = l.next;
            i++;
        }

        ListNode p1 = isStart ? l : l.next;
        r = p1;
        ListNode p2 = p1.next, t;
        while (left < right) {
            t = p2.next;
            p2.next = p1;
            p1 = p2;
            p2 = t;
            left++;
        }
        r.next = p2;
        if (isStart) {
            return p1;
        } else {
            l.next = p1;
            return head;
        }
    }

    @Test
    public void case1() {
        ListNode head = new ListNode(1);
        reverseBetween(head, 1, 1);
    }

    @Test
    public void case2() {
        ListNode l1 = new ListNode(1);
        ListNode l2 = new ListNode(2);
        ListNode l3 = new ListNode(3);
        l1.next = l2;
        l2.next = l3;
        reverseBetween(l1, 1, 2);
    }
}
