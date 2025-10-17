package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class Factorization {
    List<List<Integer>> res = new ArrayList<>();

    @Test
    public void getFactors() {
        getFactors(12);
        System.out.println(res);
    }

    public List<List<Integer>> getFactors(int n) {
        dfs(n, 2, new ArrayList<Integer>());
        return res;
    }

    private void dfs(int remain, int lastFactor, List<Integer> factors) {
        if (!factors.isEmpty()) {
            List<Integer> l = new ArrayList<>(factors);
            l.add(remain);
            res.add(l);
        }
        for (int i = lastFactor; i <= remain / i; i++) {
            if (remain % i == 0) {
                List<Integer> l = new ArrayList<>(factors);
                l.add(i);
                dfs(remain / i, i, l);
            }
        }
    }


}
