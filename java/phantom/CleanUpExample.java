package phantom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * -verbose:gc -Xmx150M
 * @author juntao zhang
 */
public class CleanUpExample {
  private InputStream input;

  {
    try {
      input = new FileInputStream("/Users/juntaozhang/src/juntaozhang/juntaozhang.github.io/java/phantom/CleanUpExample.java");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public CleanUpExample() {
    CleanUpHelper.register(this, new CleanUpImpl(input));
  }

  static class CleanUpImpl implements CleanUp {
    private final InputStream input;

    public CleanUpImpl(InputStream input) {
      this.input = input;
    }

    @Override
    public void cleanUp() {
      try {
        if (input != null) {
          input.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      System.out.println("Success!");
    }
  }

  public static void main(String[] args) throws InterruptedException {
    CleanUpExample item = new CleanUpExample();
    item = null;
    System.gc();
    Thread.sleep(2000);
  }
}
