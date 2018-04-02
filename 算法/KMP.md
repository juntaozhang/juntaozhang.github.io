# 字符串查找-KMP

http://www.lintcode.com/zh-cn/problem/strstr/

```
对于一个给定的 source 字符串和一个 target 字符串，你应该在 source 字符串中找出 target 字符串出现的第一个位置(从0开始)。如果不存在，则返回 -1。
样例
如果 source = "source" 和 target = "target"，返回 -1。
如果 source = "abcdabcdefg" 和 target = "bcd"，返回 1。
```


## 常规做法
loop source str, 每次匹配target, 一旦匹配到返回source当前位置

```java
  public int strStr(String source, String target) {
    //return source.indexOf(target);
    if (source == null || target == null) {
      return -1;
    }
    if (target.length() == 0) {
      return 0;
    }
    for (int i = 0; i < source.length(); i++) {
      boolean flag = true;
      int j;
      for (j = 0; j < target.length(); j++) {
        if ((i + j) >= source.length() || source.charAt(i + j) != target.charAt(j)) {
          flag = false;
          break;
        }
      }
      if (flag) {
        return i;
      } else if ((i + j) >= source.length()) {
        return -1;
      }
    }
    return -1;
  }
```

abaabacaba
abacaba





## 参考
* http://v.youku.com/v_show/id_XNTg3MDkwMTQ0.html
* http://v.youku.com/v_show/id_XOTI2NDQwNDE2.html