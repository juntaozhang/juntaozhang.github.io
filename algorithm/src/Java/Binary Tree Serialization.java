M

方法1: BFS. Non-recursive, using queue. 想法直观。level-order traversal. save到一个string里面就好。

方法2: DFS. Recursive. 需要一点思考。basically divide and conquer. 但是代码相对来说短。

```

/*
Design an algorithm and write code to serialize and deserialize a binary tree. Writing the tree to a file is called 'serialization' and reading back from the file to reconstruct the exact same binary tree is 'deserialization'.

There is no limit of how you deserialize or serialize a binary tree, you only need to make sure you can serialize a binary tree to a string and deserialize this string to the original structure.

Have you met this question in a real interview? Yes
Example
An example of testdata: Binary tree {3,9,20,#,#,15,7}, denote the following structure:

  3
 / \
9  20
  /  \
 15   7
Our data serialization use bfs traversal. This is just for when you got wrong answer and want to debug the input.

You can use other method to do serializaiton and deserialization.

Tags Expand 
Binary Tree Microsoft Yahoo

*/


 //BFS. store as string, separated by ','
//Note: need to record null node as well. but be careful don't push null into queue
class Solution {
    /**
     * This method will be invoked first, you should design your own algorithm 
     * to serialize a binary tree which denote by a root node to a string which
     * can be easily deserialized by your own "deserialize" method later.
     */
    public String serialize(TreeNode root) {
        String rst = "";
        if (root == null) {
            return rst;
        }
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.offer(root);
        int size = 0;
        while (!queue.isEmpty()) {
            size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                if (node.val == Integer.MIN_VALUE) {
                    rst += "#,";
                } else {
                    rst += node.val + ",";
                    TreeNode left = node.left == null ? 
                        new TreeNode(Integer.MIN_VALUE) : node.left;
                    queue.offer(left);
                    TreeNode right = node.right == null ? 
                        new TreeNode(Integer.MIN_VALUE) : node.right;
                    queue.offer(right);
                }
            }
        }
        return rst;
    }
    
    /**
     * This method will be invoked second, the argument data is what exactly
     * you serialized at method "serialize", that means the data is not given by
     * system, it's given by your own serialize method. So the format of data is
     * designed by yourself, and deserialize it here as you serialize it in 
     * "serialize" method.
     */
    public TreeNode deserialize(String data) {
        if (data == null || data.length() == 0) {
            return null;
        }
        TreeNode root = new TreeNode(0);
        root.val = Integer.parseInt(data.substring(0, data.indexOf(",")));
        data = data.substring(data.indexOf(",") + 1);
        
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.offer(root);
        int size = 0;
        while (!queue.isEmpty()) {
            size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                String temp = data.substring(0, data.indexOf(","));
                if (!temp.equals("#")) {
                    node.left = new TreeNode(Integer.parseInt(temp));
                    queue.offer(node.left);
                }                    
                data = data.substring(data.indexOf(",") + 1);
                
                temp = data.substring(0, data.indexOf(","));
                if (!temp.equals("#")) {
                    node.right = new TreeNode(Integer.parseInt(temp));
                    queue.offer(node.right);
                }
                data = data.substring(data.indexOf(",") + 1);
            }
        }
        
        return root;
    }
}












//DFS approach, recursive

/**
 * Definition of TreeNode:
 * public class TreeNode {
 *     public int val;
 *     public TreeNode left, right;
 *     public TreeNode(int val) {
 *         this.val = val;
 *         this.left = this.right = null;
 *     }
 * }
 */
class Solution {
    /**
     * This method will be invoked first, you should design your own algorithm 
     * to serialize a binary tree which denote by a root node to a string which
     * can be easily deserialized by your own "deserialize" method later.
     */
    public String serialize(TreeNode root) {
        if (root == null) {
            return "#,";
        }
        String mid = root.val + ",";
        String left = serialize(root.left);
        String right = serialize(root.right);
        mid += left + right;
        return mid;
    }
    
    private String data = "";
    /**
     * This method will be invoked second, the argument data is what exactly
     * you serialized at method "serialize", that means the data is not given by
     * system, it's given by your own serialize method. So the format of data is
     * designed by yourself, and deserialize it here as you serialize it in 
     * "serialize" method.
     */
    public TreeNode deserialize(String data) {
        this.data = data;
        return desHelper();
    }
    
    public TreeNode desHelper() {
        if (this.data.indexOf("#,") == 0) {
            this.data = this.data.substring(this.data.indexOf(",") + 1);
            return null;
        }
        String midVal = this.data.substring(0, this.data.indexOf(","));
        TreeNode mid = new TreeNode(Integer.parseInt(midVal));
        this.data = this.data.substring(this.data.indexOf(",") + 1);
        TreeNode left = desHelper();
        TreeNode right = desHelper();
        mid.left = left;
        mid.right = right;
        return mid;
    }
}


```