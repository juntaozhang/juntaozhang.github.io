package cn.juntaozhang.leetcode.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @author juntzhang
 */
public class UniqQueue<T> extends LinkedList<T> {

    private final Set<T> set = new HashSet<>();

    @Override
    public boolean offer(T e) {
        if (set.contains(e)) {
            return false;
        }
        set.add(e);
        return super.offer(e);
    }

    @Override
    public T poll() {
        T e = super.poll();
        set.remove(e);
        return e;
    }

    public static void main(String[] args) {
        Queue<Integer> q = new LinkedList<>() {
            private final Set<Integer> set = new HashSet<>();

            @Override
            public boolean offer(Integer e) {
                if (set.contains(e)) {
                    return false;
                }
                set.add(e);
                return super.offer(e);
            }

            @Override
            public Integer poll() {
                Integer e = super.poll();
                set.remove(e);
                return e;
            }

        };

        q.offer(1);
        System.out.println(q.size());
        q.offer(1);
        System.out.println(q.size());
    }
}
