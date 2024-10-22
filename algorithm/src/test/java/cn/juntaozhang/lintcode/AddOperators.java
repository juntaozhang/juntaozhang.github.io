package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class AddOperators {
    int target = 0;
    List<String> results = new ArrayList<String>();
    String num;

    @Test
    public void addOperators() {
        System.out.println(addOperators("105", 5));
    }

    public List<String> addOperators(String num, int target) {
        // Write your code here
        List<String> results = new ArrayList<String>();
        if (num == null || num.length() == 0) {
            return results;
        }
        helper(results, "", num, target, 0, 0, 0);
        return results;
    }
    public void helper(List<String> results, String path, String num, int target, int pos, long eval, long multed){
        if (pos == num.length()){
            if(target == eval)
                results.add(path);
            return;
        }
        for (int i = pos; i < num.length(); i++) {
            if (i != pos && num.charAt(pos) == '0') {
                break;
            }
            long cur = Long.parseLong(num.substring(pos, i + 1));
            if (pos == 0) {
                helper(results, path + cur, num, target, i + 1, cur, cur);
            } else {
                helper(results, path + "+" + cur, num, target, i + 1, eval + cur , cur);
                helper(results, path + "-" + cur, num, target, i + 1, eval - cur, -cur);
                helper(results, path + "*" + cur, num, target, i + 1, eval - multed + multed * cur, multed * cur );
            }
        }
    }


//    public List<String> addOperators(String num, int target) {
//        this.target = target;
//        this.num = num;
//        dfs(0, "", 0L, 0L);
//        return results;
//    }

    void dfs(int pos, String str, long res, long lastF) {
        if (num.length() == pos) {
            if (res == target) {
                results.add(str);
            }
            return;
        }
        for (int i = pos; i < num.length(); i++) {
            if (i != pos && num.charAt(pos) == '0') {
                break;
            }
            Long c = Long.valueOf(num.substring(pos, i + 1));
            if (pos == 0) {
                dfs(i + 1, str + c, c, c);
            } else {
                dfs(i + 1, str + "+" + c, res + c, c);
                dfs(i + 1, str + "-" + c, res - c, -c);
                dfs(i + 1, str + "*" + c, res - lastF + c * lastF, c * lastF);
            }
        }
    }

}
