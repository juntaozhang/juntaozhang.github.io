package cn.juntaozhang.leetcode.design;

import java.util.ArrayList;
import java.util.List;

/**
 * @author juntzhang
 */
public class StateMachine {

    static abstract class Node {

        private final String name;

        public Node(String name) {
            this.name = name;
        }

        List<Node> left = new ArrayList<>();
        List<Node> right = new ArrayList<>();

        public final boolean run(int i) {
            boolean ans = run0(i);
            System.out.println(name + " run=" + ans);
            return ans;
        }

        abstract boolean run0(int i);
    }

    static class Pipeline {

        private final Node root;

        public Pipeline(Node root) {
            this.root = root;
        }

        public boolean run(int i) {
            return root.run(i);
        }
    }

    static class PipelineFactory {

        // build from config
        public static Pipeline create() {
            Node n1 = new Node("A") {
                @Override
                public boolean run0(int i) {
                    if (i > 10) {
                        return left.stream().allMatch(child -> child.run(i));
                    } else {
                        return right.stream().allMatch(child -> child.run(i));
                    }
                }
            };

            Node n2 = new Node("B") {
                @Override
                public boolean run0(int i) {
                    if (i > 20) {
                        return left.stream().allMatch(child -> child.run(i));
                    } else {
                        return false;
                    }
                }
            };

            Node n3 = new Node("C") {
                @Override
                public boolean run0(int i) {
                    return i % 2 == 0;
                }
            };

            n1.left.add(n2);
            n1.right.add(n3);

            n2.left.add(n3);
            n2.right.clear();

            n3.left.clear();
            n3.right.clear();
            return new Pipeline(n1);
        }
    }

    public static void main(String[] args) {
        Pipeline pipeline = PipelineFactory.create();
        System.out.println(pipeline.run(40));
        System.out.println(pipeline.run(41));
        System.out.println(pipeline.run(10));
        System.out.println(pipeline.run(8));
        System.out.println(pipeline.run(7));
    }


}
