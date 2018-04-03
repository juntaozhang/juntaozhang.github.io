# 字符串查找-KMP

http://www.lintcode.com/zh-cn/problem/strstr/

```
对于一个给定的 source 字符串和一个 target 字符串，你应该在 source 字符串中找出 target 字符串出现的第一个位置(从0开始)。如果不存在，则返回 -1。
样例
如果 source = "source" 和 target = "target"，返回 -1。
如果 source = "abcdabcdefg" 和 target = "bcd"，返回 1。
```


## 常规做法
loop source str, 每次匹配`模式串target`(m), 一旦匹配到返回`文本串source`(n)当前位置

```java
    public int strStr(String source, String target) {
        int i = 0, j = 0;
        if (source == null || target == null) {
            return -1;
        }
        if (target.length() == 0) {
            return 0;
        }
        while(i < source.length() && j < target.length()) {
            if(source.charAt(i) == target.charAt(j)){
                i++;
                j++;
            } else {
                i=i-j+1;
                j=0;
            }
        }
        if(j==target.length()){
            return i-j;
        }else{
            return -1;
        }
    }
```

>时间复杂度:n * m


>abaabacaba    
>abacaba

我们发现中间有很多匹配是可以跳过

## KMP

### 挖掘模式串规律

```txt
	i		0	1	2	3	4	5	6	7	8
模式串		a	b	a	a	b	c	a	b	a
next		-1	0	0	1	1	2	0	1	2
newnext	-1	0	-1	1	0	2	-1	0	-1	
									
i=0				-1						
i=1		a		0						
i=2		ab		0						
i=3		aba		1	a…	…a				
i=4		abaa	1	a…	…a				
i=5		abaab	2	ab…	…ab				
i=6		abaabc	0						
```
next如何求值:

```c
typedef char* String;
void get_next(String target,int next[],int size){
    next[0]=-1;
    int i=0;
    int j=-1;
    while (i<size-1) {
        if(-1==j||target[i]==target[j]){
            i++;
            j++;
            next[i]=j;
        }else{
            j=next[j];
        }
    }
}
```
改进之后next值:

```c
typedef char* String;
void get_next(String target,int next[],int size){
    next[0]=-1;
    int i=0;
    int j=-1;
    while (i<size-1) {
        if(-1==j||target[i]==target[j]){
            i++;
            j++;
            if(target[i]==target[j]){
                next[i]=next[j];
            }else{
                next[i]=j;
            }
        }else{
            j=next[j];
        }
    }
}
```
对与字符串位置i


```java
  public int strStr(String source, String target) {
    int i = 0, j = 0;
    if (source == null || target == null) {
      return -1;
    }
    if (target.length() == 0) {
      return 0;
    }
    int[] next = getNext(target);
    while (i < source.length() && j < target.length()) {
      if (j==-1 || source.charAt(i) == target.charAt(j)) {
        i++;
        j++;
      } else {
        j = next[j];
      }
    }
    if (j == target.length()) {
      return i - j;
    } else {
      return -1;
    }
  }

  private int[] getNext(String target) {
    int[] next = new int[target.length()];
    next[0] = -1;
    int i = 0;
    int j = -1;
    while (i < next.length - 1) {
      if (-1 == j || target.charAt(i) == target.charAt(j)) {
        i++;
        j++;
        if (target.charAt(i) == target.charAt(j)) {
          next[i] = next[j];
        } else {
          next[i] = j;
        }
      } else {
        j = next[j];
      }
    }
    return next;
  }
```






## 参考
* http://v.youku.com/v_show/id_XNTg3MDkwMTQ0.html
* http://v.youku.com/v_show/id_XOTI2NDQwNDE2.html