# JSSE-SSL

## 基本概念
- keyStone: 密钥存储库,公钥私钥证书
- TrustStore: 信任密钥存储库,CA公钥

## java
### keytool
- list
`keytool -list -keystore cacerts`
mac_jre_security_cacerts : /Library/Java/JavaVirtualMachines/jdk1.8.0_73.jdk/Contents/Home/jre/lib/security/cacerts

- import 
`keytool -import -file <XXXX.cer> -alias updates.jenkins.io -keystore cacerts`
默认密码:changeit

- 导入之后java test
```java
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
public class CheckURL {
  public static void main(String[] args) throws Exception {
    String src = "https://updates.jenkins.io/update-center.json?id=default&version=2.89.4";
    URLConnection  con = new URL(src).openConnection();
    try (InputStream is = con.getInputStream()) {
      byte[] tmp = new byte[1024*10];
      is.read(tmp);
      System.out.println(new String(tmp));
    }
  }
}
```

### openssl

openssl s_client -connect updates.jenkins.io:443

### mac 导出证书

(1).Open Finder and go to Applications -> Utilities -> Keychain Access
(2).Double click on Keychain to open it
(3).On the left, click on Certificates
(4).Highlight the certificate to export and open File -> Export
(5).Choose an appropriate filename (mycert.pfx) and directory. 



### 参考

- https://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html
- https://www.ibm.com/developerworks/cn/java/j-lo-socketkeytool/index.html?ca=drs
- ./其他/SSL-TLS.md