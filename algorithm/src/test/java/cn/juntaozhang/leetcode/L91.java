package cn.juntaozhang.leetcode;

import org.junit.Test;

public class L91 {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode cur = head, pre = null, next = null, start = cur, end = cur;
        for (int i = 1; i <= right; i++) {
            if (i == left - 1) {
                pre = cur;
            }
            if (i == right) {
                next = cur.next;
            }
            if (i == left) {
                end = cur;
            }
            if (left <= i && i <= right) {
                ListNode tmp = cur.next;
                cur.next = start;
                start = cur;
                cur = tmp;
            } else {
                cur = cur.next;
            }
        }
        if (pre != null) {
            pre.next = start;
        } else {
            head = start;
        }
        end.next = next;
        return head;
    }

    @Test
    public void case1() {
        ListNode head = new ListNode(1, new ListNode(2, new ListNode(3, new ListNode(4, new ListNode(5, new ListNode(6))))));
        ListNode res = reverseBetween(head, 2, 4);
        while (res != null) {
            System.out.println(res.val);
            res = res.next;
        }
    }

    @Test
    public void case2() {
        ListNode head = new ListNode(3,new ListNode(5));
        ListNode res = reverseBetween(head, 1, 2);
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
