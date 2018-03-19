import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author juntao zhang
 */
public class CheckURL {

  static String host = "updates.jenkins.io";
  static String src = "https://" + host + "/update-center.json?id=default&version=2.89.4";
  static char[] passphrase = "changeit".toCharArray();
  static String JAVA_HOME = "/Library/Java/JavaVirtualMachines/jdk1.8.0_73.jdk/Contents/Home/";

  public static void main(String[] args) throws Exception {
    store();
    read();
  }

  private static void checkURL() throws IOException {
    URLConnection con = new URL(src).openConnection();
    try (InputStream is = con.getInputStream()) {
      byte[] tmp = new byte[1024];
      is.read(tmp);
      System.out.println(new String(tmp));
    }
  }

  private static void read() throws Exception {
    File file = new File("mycacerts");
    InputStream in = new FileInputStream(file);
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(in, passphrase);
    in.close();

    SSLContext sslContext = SSLContext.getInstance("TLS");
    TrustManagerFactory tmf = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);
    sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
    SSLSocketFactory factory = sslContext.getSocketFactory();
    HttpsURLConnection.setDefaultSSLSocketFactory(factory);
    try {
      checkURL();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void store() throws Exception {
    File file = new File("mycacerts");
    if(file.exists()){
      file.delete();
    }
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null);

    SSLContext sslContext = SSLContext.getInstance("TLS");
    TrustManagerFactory tmf = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);
    X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
    SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
    sslContext.init(null, new TrustManager[]{tm}, null);
    SSLSocketFactory factory = sslContext.getSocketFactory();
    HttpsURLConnection.setDefaultSSLSocketFactory(factory);
    try {
      checkURL();
    } catch (Exception ignore) {
    }
    X509Certificate cert = tm.chain[0];
    String alias = host;
    ks.setCertificateEntry(alias, cert);
    OutputStream out = new FileOutputStream("mycacerts");
    ks.store(out, passphrase);
    out.close();
  }

  private static class SavingTrustManager implements X509TrustManager {

    private final X509TrustManager tm;
    private X509Certificate[] chain;

    SavingTrustManager(X509TrustManager tm) {
      this.tm = tm;
    }

    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      throw new UnsupportedOperationException();
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      this.chain = chain;
      tm.checkServerTrusted(chain, authType);
    }
  }

  private static File getFile() {
    File file = new File("mycacerts");
    if (!file.isFile()) {
      char SEP = File.separatorChar;
      File dir = new File(System.getProperty("java.home") + SEP + "lib"
          + SEP + "security");
      file = new File(dir, "mycacerts");
      if (!file.isFile()) {
        file = new File(dir, "cacerts");
      }
    }
    return file;
  }

}
