package cn.juntaozhang.leetcode.bytedance;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class 化学公式解析 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String formula = scanner.nextLine();
        System.out.println(new 化学公式解析().parse(formula));
    }

    public String parse(String formula) {
        Map<String, Integer> elementCounts = new HashMap<>();
        Stack<Object> stack = new Stack<>();
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (Character.isUpperCase(c)) {
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                while (i + 1 < formula.length() && Character.isLowerCase(formula.charAt(i + 1))) {
                    sb.append(formula.charAt(i + 1));
                    i++;
                }
                stack.push(sb.toString());
            } else if (Character.isDigit(c)) {
                int num = c - '0';
                while (i + 1 < formula.length() && Character.isDigit(formula.charAt(i + 1))) {
                    num = num * 10 + formula.charAt(i + 1) - '0';
                    i++;
                }
                stack.push(num);
            } else if (c == '[' || c == '(') {
                stack.push(c + "");
            } else if (c == ']' || c == ')') {
                Map<String, Integer> group = new HashMap<>();
                List<Object> elements = new ArrayList<>();
                Object elem = stack.pop();
                while (!elem.equals("(") && !elem.equals("[")) {
                    if (elem instanceof Map) {
                        group.putAll((Map) elem);
                    } else {
                        elements.add(elem);
                    }
                    elem = stack.pop();
                }
                Collections.reverse(elements);
                for (int j = 0; j < elements.size(); j++) {
                    if (elements.get(j) instanceof String) {
                        String element = (String) elements.get(j);
                        int count = 1;
                        if (j + 1 < elements.size() && elements.get(j + 1) instanceof Integer) {
                            count = (int) elements.get(j + 1);
                            j++;
                        }
                        group.put(element, group.getOrDefault(element, 0) + count);
                    }

                }
                int num = 0;
                while (i + 1 < formula.length() && Character.isDigit(formula.charAt(i + 1))) {
                    num = num * 10 + formula.charAt(i + 1) - '0';
                    i++;
                }
                for (Map.Entry<String, Integer> entry : group.entrySet()) {
                    group.put(entry.getKey(), entry.getValue() * Math.max(num, 1));
                }
                stack.push(group);
            }
        }
        while (!stack.isEmpty()) {
            Object elem = stack.pop();
            if (elem instanceof Map) {
                for (Map.Entry<String, Integer> entry : ((Map<String, Integer>) elem).entrySet()) {
                    elementCounts.put(entry.getKey(), elementCounts.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            } else if (elem instanceof String) {
                elementCounts.put((String) elem, elementCounts.getOrDefault(elem, 0) + 1);
            } else if (elem instanceof Integer) {
                String k = (String) stack.pop();
                elementCounts.put(k, elementCounts.getOrDefault(k, 0) + (Integer) elem);
            }
        }
        StringBuilder sb = new StringBuilder();
        elementCounts.keySet().stream().sorted().forEach(k -> {
            sb.append(k).append(elementCounts.get(k));

        });
        return sb.toString();
    }

    @Test
    public void case1() {
        Assert.assertEquals("H2O1", parse("H2O"));
    }

    @Test
    public void case2() {
        Assert.assertEquals("H2Mg1O2", parse("Mg(OH)2"));
    }

    @Test
    public void case3() {
        Assert.assertEquals("K4N2O14S4", parse("K4[ON(SO3)2]2"));
    }

    @Test
    public void case4() {
        Assert.assertEquals("Ca1K1Na1", parse("(CaK)Na"));
    }
}
