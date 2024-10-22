M

方法1：   
题目要求DFS。   
其实basic implementation. 每次处理node.left.next = node.right; node.right.next = node.next.left;


方法2:   
不和题意，用了queue space，与Input成正比。太大。

BFS over Tree。 用Queue 和 queue.size()，老规矩。   
process每层queue时, 注意把next pointer加上去就好. 

```
/*
Given a binary tree

    struct TreeLinkNode {
      TreeLinkNode *left;
      TreeLinkNode *right;
      TreeLinkNode *next;
    }
Populate each next pointer to point to its next right node. If there is no next right node, the next pointer should be set to NULL.

Initially, all next pointers are set to NULL.

Note:

You may only use constant extra space.
You may assume that it is a perfect binary tree (ie, all leaves are at the same level, and every parent has two children).
For example,
Given the following perfect binary tree,
         1
       /  \
      2    3
     / \  / \
    4  5  6  7
After calling your function, the tree should look like:
         1 -> NULL
       /  \
      2 -> 3 -> NULL
     / \  / \
    4->5->6->7 -> NULL
Hide Tags Tree Depth-first Search
Hide Similar Problems (H) Populating Next Right Pointers in Each Node II (M) Binary Tree Right Side View

*/


/**
 * Definition for binary tree with next pointer.
 * public class TreeLinkNode {
 *     int val;
 *     TreeLinkNode left, right, next;
 *     TreeLinkNode(int x) { val = x; }
 * }
 */

//DFS. Basic implementation according to problem.
 public class Solution {
    public void connect(TreeLinkNode root) {
        if (root == null || (root.left == null && root.right == null)) {
            return;
        }
        root.left.next = root.right;
        dfs(root.left);
        dfs(root.right);
    }
    
    public void dfs(TreeLinkNode node) {
        if (node == null || node.left == null || node.right == null) {
            return;
        }
        node.left.next = node.right;
        if (node.next != null)
            node.right.next = node.next.left;
        dfs(node.left);
        dfs(node.right);
    }
}




 //BFS, However, 不和题意。 point each node to the next in queue. if none, add null
public class Solution {
    public void connect(TreeLinkNode root) {
        if (root == null || (root.left == null && root.right == null)) {
            return;
        }
        Queue<TreeLinkNode> queue = new LinkedList<TreeLinkNode>();
        queue.offer(root);
        int size = 0;
        
        while (!queue.isEmpty()) {
            size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeLinkNode node = queue.poll();
                if (i < size - 1) {
                    node.next = queue.peek();
                } else {
                    node.next = null;
                }
                if (node.left != null) {
                    queue.offer(node.left);
                }
                if (node.right != null) {
                    queue.offer(node.right);
                }
            }
            size = queue.size();
        }
    }
}
```