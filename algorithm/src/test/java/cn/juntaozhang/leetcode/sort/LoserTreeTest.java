package cn.juntaozhang.leetcode.sort;

import cn.juntaozhang.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class LoserTreeTest {
  public static List<Integer> merge(List<List<Integer>> sortedArrays) {
    List<Integer> result = new ArrayList<>(sortedArrays.size());
    int arrSize = sortedArrays.size();
    int[] pointers = new int[arrSize];
    Arrays.fill(pointers, 0);
    while (true) {
      int minIndex = -1;
      int minValue = Integer.MAX_VALUE;
      for (int i = 0; i < arrSize; i++) {
        if (pointers[i] < sortedArrays.get(i).size()
            && minValue > sortedArrays.get(i).get(pointers[i])) {
          minValue = sortedArrays.get(i).get(pointers[i]);
          minIndex = i;
        }
      }
      if (minIndex != -1) {
        pointers[minIndex]++;
        result.add(minValue);
      } else {
        break;
      }
    }
    return result;
  }

  @Test
  public void test() {
    List<List<Integer>> arrays = new ArrayList<>();
    arrays.add(Arrays.asList(1, 6, 11, 15));
    arrays.add(Arrays.asList(2, 7, 9, 10));
    arrays.add(Arrays.asList(3, 8));
    arrays.add(Arrays.asList(0, 11, 12, 13, 14));
    StringUtils.print(merge(arrays));
  }
}
