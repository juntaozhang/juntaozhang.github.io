package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class L166 {
    public String fractionToDecimal(int _numerator, int _denominator) {
        long numerator = _numerator;
        long denominator = _denominator; // 防止分子分母出现负数转正数-2147483648超过int范围
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        LinkedList<String> list = new LinkedList<String>();
        if (denominator == 0) {
            throw new RuntimeException("denominator can't be 0!");
        }
        // -22/-3
        if (denominator < 0) {
            denominator = -denominator;
            numerator = -numerator;
        }
        // -22/3
        if (numerator < 0) {
            list.add("-");
            numerator = -numerator;
        }
        // 22/3
        if (numerator >= denominator) {
            // 4 / 3
            list.add(String.valueOf(numerator / denominator));
            numerator %= denominator;
        } else {
            list.add("0");
        }

        if (numerator != 0) {
            list.add(".");
        }

        while (numerator != 0) {
            numerator *= 10;
            Integer i = map.get(numerator);
            if (i != null) {
                list.add(i, "(");
                list.add(")");
                break;
            }
            if (list.size() >= 10000) {
                break;
            }
            map.put(numerator, list.size());
            list.add(String.valueOf(numerator / denominator));
            numerator %= denominator;
        }
        StringBuilder sb = new StringBuilder();
        while (!list.isEmpty()) {
            sb.append(list.poll());
        }
        return sb.toString();
    }

    @Test
    public void case1() {
        Assert.assertEquals("123.(012)", fractionToDecimal(4 + 333 * 123, 333));
        Assert.assertEquals("0.(012)", fractionToDecimal(4, 333));
        Assert.assertEquals("0.(3)", fractionToDecimal(1, 3));
    }

    @Test
    public void case2() {
        Assert.assertEquals("-6.25", fractionToDecimal(-50, 8));
    }

    @Test
    public void case3() {
        Assert.assertEquals("11", fractionToDecimal(-22, -2));
    }

    @Test
    public void case4() {
        Assert.assertEquals("0.0000000004656612873077392578125", fractionToDecimal(-1, -2147483648));
    }
}
