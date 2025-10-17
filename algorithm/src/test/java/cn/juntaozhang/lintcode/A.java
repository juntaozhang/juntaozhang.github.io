package cn.juntaozhang.lintcode;

import com.google.common.base.MoreObjects;
import org.junit.Test;

/**
 * 
 */
public class A {
    class Tree {

        public int x;
        public Tree l;
        public Tree r;

        public Tree(int x, Tree l, Tree r) {
            this.x = x;
            this.l = l;
            this.r = r;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("x", x)
                    .add("l", l)
                    .add("r", r)
                    .toString();
        }
    }


    int rst = 0;
    java.util.HashSet<Integer> set = new java.util.HashSet<Integer>();

    public int solution(Tree T) {
        if (T == null) {
            return 0;
        }
        dfs(T);
        return rst;
    }

    private void dfs(Tree T) {
        //leaf
        if (T.l == null && T.r == null) {
            if (!set.add(T.x)) {
                return;
            }
            rst = Math.max(rst, set.size());
            set.remove(T.x);
        }
        //left tree
        if (T.l != null) {
            if (!set.add(T.x)) {
                return;
            }
            dfs(T.l);
            set.remove(T.x);
        }
        if (T.r != null) {
            if (!set.add(T.x)) {
                return;
            }
            dfs(T.r);
            set.remove(T.x);
        }
    }

    @Test
    public void test() {

//        System.out.println(Sets.newHashSet(1,2).add(3));
        Tree t = new Tree(4,
                new Tree(5,
                        new Tree(
                                3, null, null),
                        null
                ),
                new Tree(6,
                        new Tree(1,
                                new Tree(6,null,null),
                                new Tree(2,null,null)
                        ),
                        null
                )
        );
        System.out.println(solution(t));
    }

    @Test
    public void test2() {
        Tree t = new Tree(4, null, null);
        System.out.println(solution(t));
    }

    @Test
    public void test3() {
        System.out.println(solution(null));
    }

}
