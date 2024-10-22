Merge sort:
    1. find middle. 快慢指针
    2. Merge:  假设given list A, B 已经是sorted, 然后按照大小，混合。
    3. Sort: 切开两半，先sort前半, 如果先sort了mid.next~end, sort后，中间点mid.next == null，再sort前半段。
        然后mege.
        要recursively call itself.

Quick sort:
想做可以看讲义：http://www.jiuzhang.com/solutions/sort-list/

但是quick sort不建议用在list上面。

排列list, merge sort可能更可行和合理。原因分析在下面， 以及： http://www.geeksforgeeks.org/why-quick-sort-preferred-for-arrays-and-merge-sort-for-linked-lists/

```
/*
28% Accepted
Sort a linked list in O(n log n) time using constant space complexity.

Example
Given 1-3->2->null, sort it to 1->2->3->null.

Tags Expand 
Linked List


*/

/**
 * Definition for ListNode.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode(int val) {
 *         this.val = val;
 *         this.next = null;
 *     }
 * }
 */ 




/*
    Recap:12.09.2015. practice merge sort
Thinking process:
1.Divide and conquer
2. can use merge sort or quick sort. Used merge sort here.
3. Find middle
4. Sort each side recursively.
5. merge function.
Note: when checking null, node != null should be in front of node.next != null. Because if node is alreay null, node.next gives exception.

*/



public class Solution {
    public ListNode findMiddle(ListNode head) {
        ListNode slow = head;
        ListNode fast = head.next;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
    
    public ListNode merge(ListNode left, ListNode right) {
        ListNode dummy = new ListNode(0);
        ListNode head = dummy;
        while (left != null && right != null) {
            if (left.val < right.val) {
                head.next = left;
                left = left.next;
            } else {
                head.next = right;
                right = right.next;
            }
            head = head.next;
        }
        if (left != null) {
            head.next = left;
        } else if (right != null){
            head.next = right;
        }
        return dummy.next;
    }
    
    /*
     * @param head: The head of linked list.
     * @return: You should return the head of the sorted linked list,
                    using constant space complexity.
     */
    public ListNode sortList(ListNode head) {  
        if (head == null || head.next == null) {
            return head;
        }
        ListNode mid = findMiddle(head);
        ListNode left = sortList(mid.next);
        mid.next = null;
        ListNode right = sortList(head);
        return merge(left, right);
    }
    

}


/*
    12.09.2015:  http://www.jiuzhang.com/solutions/sort-list/
    Quick sort. 

    Didn't do this myself yet. This is because Quick sort in general is not so good for list.
    Quick sort requires a lot random access to elements, which turns into worst case O(n) time to access a element 
    in list. So it can be worse to o(n^2).
    So here Merge sort is prefered.

    Quick sort is prefered for array sort, partition.

    Merge sort is prefered for list sort
*/

public class Solution {
    /*
     * @param head: The head of linked list.
     * @return: You should return the head of the sorted linked list,
                    using constant space complexity.
     */
    public ListNode sortList(ListNode head) {  
        // write your code here
    }
}

```