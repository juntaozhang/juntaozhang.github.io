package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.*;

/**
 * 
 */
public class SubarraySumClosest {
    @Test
    public void subarraySumClosest() {
        System.out.println(Arrays.toString(subarraySumClosest(new int[]{5,10,5,3,2,1,1,-2,-4,3})));
    }

    public int[] subarraySumClosest(int[] nums) {
        int[] res = new int[2];
        int closet = Integer.MAX_VALUE;
        int sum = 0;
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];
            if (map.get(sum) != null) {
                res[0] = map.get(sum) + 1;//sum[i] -sum[j] i不能算
                res[1] = i;
                return res;
            }
            map.put(sum, i);
        }
        List<Integer> list = new ArrayList<>(map.keySet());
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        int pre = list.get(0);
        for (int i = 1; i < list.size(); pre = list.get(i), i++) {
            if (Math.abs(pre - list.get(i)) < closet) {
                res[0] = map.get(pre);
                res[1] = map.get(list.get(i));
                if (res[0] > res[1]) {
                    int t = res[0];
                    res[0] = res[1];
                    res[1] = t;
                }
                res[0]++;//sum[i] -sum[j] i不能算
                closet = Math.abs(pre - list.get(i));
            }
        }
        return res;
    }
}
