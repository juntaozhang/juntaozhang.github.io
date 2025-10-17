package cn.juntaozhang.leetcode;

import org.junit.Assert;
import org.junit.Test;

public class L1739 {
    public int minimumBoxes2(int n) {
        int cur = 1, i = 1, j = 1;
        while (n > cur) {
            n -= cur;
            i++;
            cur += i;
        }
        System.out.println(n + " " + i);
        cur = 1;
        while (n > cur) {
            n -= cur;
            j++;
            cur++;
        }
        return (i - 1) * i / 2 + j;
    }

    public int minimumBoxes(int n) {
        int s = 1;
        int i = 1;
        while (true) {
            i++;
            int t = (i + 1) * i / 2;
            s += t;
            if (s > n) {
                s -= t;
                break;
            }
        }

        s = n - s;
        System.out.println(s + " " + i);
        int j = 0;
        if (s > 0) {
            while (true) {
                j++;
                if (s <= j) {
                    break;
                }
                s -= j;
            }
        }
        return (i - 1) * i / 2 + j;
    }

    @Test
    public void case1() {
        Assert.assertEquals(9, minimumBoxes(15));
    }

    @Test
    public void case2() {
        Assert.assertEquals(21, minimumBoxes(51));
    }

    @Test
    public void case3() {
        Assert.assertEquals(39, minimumBoxes2(126));
        Assert.assertEquals(39, minimumBoxes(126));
    }
}
