package phantom;

import java.util.concurrent.TimeUnit;

/**
 * jmap -histo:live {pid}
 * -XX:+PrintGCDetails
 * @author juntao zhang
 */
public class FinalizerTest {

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    System.out.println("finalize.");
  }

  /**
   * 有finalize(4733606ns) vs 无(630689ns)
   */
  public static void performance() {
    long start = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
      FinalizerTest finalizerTest = new FinalizerTest();

    }
    System.out.println(System.nanoTime() - start);
  }

  public static void main(String[] args) throws InterruptedException {
    FinalizerTest finalizerTest = new FinalizerTest();
    finalizerTest = null;
    System.out.println("system gc");
    System.gc();
    TimeUnit.MILLISECONDS.sleep(100);
    System.out.println("main end");
  }
}
