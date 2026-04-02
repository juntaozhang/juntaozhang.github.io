package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

public class L191 {
    public int hammingWeight(int n) {
        int cnt = 0;
        while (n != 0) {
            if (n % 2 == 1) {
                cnt++;
            }
            // n = n / 2;
            n = n >> 1;
        }
        return cnt;
    }

    public int hammingWeight2(int n) {
        int cnt = 0;
        while (n != 0) {
            n &= n - 1;
            cnt++;
        }
        return cnt;
    }

    @Test
    public void case1() {
        Assert.assertEquals(3, hammingWeight(11));
    }

    @Test
    public void case2() {
        System.out.println(Integer.toBinaryString(6));
        System.out.println(Integer.toBinaryString(5));
        System.out.println(Integer.toBinaryString(6 & 5)); // 其运算结果恰为把 n 的二进制位中的最低位的 1 变为 0 之后的结果。
        Assert.assertEquals(1, hammingWeight2(128));
    }
}
