package cn.juntaozhang.jdk;

/**
 * “龟兔赛跑算法”或“弗洛伊德环检测算法”
 */
public class LinkedListLoopDetect {

    Node head;  // head of list

    /* Linked list Node*/
    class Node {
        int data;
        Node next;

        Node(int d) {
            data = d;
            next = null;
        }
    }

    /* Inserts a new Node at front of the list. */
    public void push(int new_data) {
        /* 1 & 2: Allocate the Node &
                  Put in the data*/
        Node new_node = new Node(new_data);

        /* 3. Make next of new Node as head */
        new_node.next = head;

        /* 4. Move the head to point to new Node */
        head = new_node;
    }

    /**
     * 这是一个检测链表中是否存在环的算法，通常称为“龟兔赛跑算法”或“弗洛伊德环检测算法”。
     * 该算法使用两个指针（一个快指针和一个慢指针）来遍历链表。如果链表中存在环，快指针和慢指针最终会相遇。
     */
    int detectLoop() {
        Node slow_p = head, fast_p = head;
        while (slow_p != null && fast_p != null && fast_p.next != null) {
            slow_p = slow_p.next;
            fast_p = fast_p.next.next;
            if (slow_p == fast_p) {
                System.out.println("Found loop");
                return 1;
            }
        }
        return 0;
    }

    /* Drier program to test above functions */
    public static void main(String args[]) {
        LinkedListLoopDetect l = new LinkedListLoopDetect();

        l.push(20);
        l.push(4);
        l.push(15);
        l.push(10);

        System.out.println(l.detectLoop());

        /*Create loop for testing */
        l.head.next.next.next.next = l.head.next;

        System.out.println(l.detectLoop());
    }

}
