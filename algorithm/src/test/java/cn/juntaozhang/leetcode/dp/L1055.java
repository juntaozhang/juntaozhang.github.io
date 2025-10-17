package cn.juntaozhang.leetcode.dp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author juntzhang
 */
public class L1055 {

    public int shortestWay(String source, String target) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        int res = 1;
        List<Integer> tmp = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            int finalI = i;
            map.compute((int) source.charAt(i), (_k, _v) -> {
                if (_v == null) {
                    _v = new ArrayList<>();
                }
                _v.add(finalI);
                return _v;
            });
        }

        for (int i = 0; i < target.length(); i++) {
            List<Integer> list = map.get((int) target.charAt(i));
            if (list == null) {
                return -1;
            } else {
                if (tmp.size() == 0) {
                    tmp.add(list.get(0));
                } else {
                    int last = tmp.get(tmp.size() - 1);
                    // 贪心找到最小
                    boolean flag = false;
                    for (int j : list) {
                        if (last < j) {
                            tmp.add(j);
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        // 没有找到
                        tmp.clear();
                        tmp.add(list.get(0));
                        res += 1;
                    }

                }
            }
        }

        return res;
    }

    public static void main(String[] args) {
//        System.out.println(new L1055().shortestWay("abc", "abcbc"));
//        System.out.println(new L1055().shortestWay("abc", "a"));
        System.out.println(new L1055().shortestWay("xyxz", "xzyxz"));
    }
}
