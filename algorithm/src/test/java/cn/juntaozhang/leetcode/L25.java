package cn.juntaozhang.leetcode;

import org.junit.Test;

public class L25 {
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode cur = head, pre = null;
        int len = size(head);
        for (int i = k; i <= len; i += k) {
            ListNode[] arr = reverseK(pre, cur, k);
            if (i == k) head = arr[0];
            pre = arr[1];
            cur = arr[2];
        }
        return head;
    }

    public int size(ListNode cur) {
        int i = 0;
        while (cur != null) {
            i++;
            cur = cur.next;
        }
        return i;
    }

    public ListNode[] reverseK(ListNode pre, ListNode cur, int k) {
        // make sure cur length is large than k
        ListNode left = cur, next = cur.next;
        for (int i = 1; i < k; i++) {
            ListNode tmp = cur;
            cur = cur.next;
            next = cur.next;

            cur.next = left;
            left = cur;
            cur = tmp;
            cur.next = next;
        }
        if (pre != null) pre.next = left;
        return new ListNode[]{left, cur, next};
    }

    @Test
    public void case1() {
        ListNode head = new ListNode(1, new ListNode(2, new ListNode(3, new ListNode(4, new ListNode(5)))));
        ListNode res = reverseKGroup(head, 2);
        while (res != null) {
            System.out.println(res.val);
            res = res.next;
        }
    }

    public class ListNode {
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
}
