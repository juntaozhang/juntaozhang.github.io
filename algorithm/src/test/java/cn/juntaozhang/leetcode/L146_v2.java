package cn.juntaozhang.leetcode;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 按照最近使用排序
 * 
 */
public class L146_v2 {
  /*
      输入
      ["LRUCache", "put", "put", "get", "put", "get", "put", "get", "get", "get"]
      [[2], [1, 1], [2, 2], [1], [3, 3], [2], [4, 4], [1], [3], [4]]
      输出
      [null, null, null, 1, null, -1, null, -1, 3, 4]
   */
  @Test
  public void case1() {
    LRUCache lRUCache = new LRUCache(2);
    lRUCache.put(1, 1); // 缓存是 {1=1}
    lRUCache.put(2, 2); // 缓存是 {1=1, 2=2}
    lRUCache.get(1);    // 返回 1
    lRUCache.put(3, 3); // 该操作会使得关键字 2 作废，缓存是 {1=1, 3=3}
    lRUCache.get(2);    // 返回 -1 (未找到)
    lRUCache.put(4, 4); // 该操作会使得关键字 1 作废，缓存是 {4=4, 3=3}
    lRUCache.get(1);    // 返回 -1 (未找到)
    lRUCache.get(3);    // 返回 3
    lRUCache.get(4);    // 返回 4
  }

  @Test
  public void case2() {
    LRUCache cache = new LRUCache(10);
    for (String a : "10,13],[3,17],[6,11],[10,5],[9,10],[13],[2,19],[2],[3],[5,25],[8],[9,22],[5,5],[1,30],[11],[9,12],[7],[5],[8],[9],[4,30],[9,3],[9],[10],[10],[6,14],[3,1],[3],[10,11],[8],[2,14],[1],[5],[4],[11,4],[12,24],[5,18],[13],[7,23],[8],[12],[3,27],[2,12],[5],[2,9],[13,4],[8,18],[1,7],[6],[9,29],[8,21],[5],[6,30],[1,12],[10],[4,15],[7,22],[11,26],[8,17],[9,29],[5],[3,4],[11,30],[12],[4,29],[3],[9],[6],[3,4],[1],[10],[3,29],[10,28],[1,20],[11,13],[3],[3,12],[3,8],[10,9],[3,26],[8],[7],[5],[13,17],[2,27],[11,15],[12],[9,19],[2,15],[3,16],[1],[12,17],[9,1],[6,19],[4],[5],[5],[8,1],[11,7],[5,2],[9,28],[1],[2,2],[7,4],[4,22],[7,24],[9,26],[13,28],[11,26".split("],\\[")) {
      if ("1".equals(a)) {
        System.out.print("=== ");
      }
      String[] b = a.split(",");
      if (b.length == 1) {
        System.out.print("get " + a);
        System.out.println(" => " + cache.get(Integer.valueOf(b[0])));
      } else {
        System.out.print("put " + a);
        cache.put(Integer.valueOf(b[0]), Integer.valueOf(b[1]));
        System.out.println("null");
      }
      System.out.println(cache.toString());
    }
  }

  static class LRUCache extends LinkedHashMap<Integer, Integer> {
    private final int capacity;

    public LRUCache(int capacity) {
      super(capacity, 0.75f, true);
      this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
      return size() > this.capacity;
    }
  }
}
