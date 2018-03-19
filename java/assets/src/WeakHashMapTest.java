package src;

import java.util.WeakHashMap;

/**
 * @author juntao zhang
 */
public class WeakHashMapTest {
  public static void main(String[] args) {
    Thread t1 = new Thread(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignore) {
      }
    });

    WeakHashMap<Thread, String> map = new WeakHashMap<>();
    map.put(t1, "metadata");
    t1.start();
    t1 = null;
    System.gc();
    System.out.println(map);//数据还在,thread 还活着
    try {
      Thread.sleep(1500);
    } catch (InterruptedException ignore) {
    }
    System.out.println(map);//数据还在,thread销毁但没有触发GC
    System.gc();
    System.out.println(map);//数据被回收
  }
}
