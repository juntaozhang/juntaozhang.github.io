## TODO
* VIRT-java程序VIRT过大问题.md


## java集合
### Map
* [WeakReference与WeakHashMap](/java/WeakReference与WeakHashMap.md)
* [关于 HashMap 1.8 的重大更新](http://blog.csdn.net/carson_ho/article/details/79373134)
* [彻头彻尾理解 LinkedHashMap](http://blog.csdn.net/justloveyou_/article/details/71713781)

### LinkedHashMap
* 1.Entry 是双链表
* 2.Entry可以定义两种顺序：插入顺序和LRU顺序
[LinkedHashMapInsertTest](https://gitee.com/zjt_hans/hello-java/JavaSE/src/main/java/org/example/java/util/colls/LinkedHashMapInsertTest.java)
[LinkedHashMapLRUTest](https://gitee.com/zjt_hans/hello-java/JavaSE/src/main/java/org/example/java/util/colls/LinkedHashMapLRUTest.java)

## java 死锁
[通过MXBean程序查找死锁](https://gitee.com/zjt_hans/hello-java/blob/master/JavaSE/src/main/java/org/example/java/jvm/monitoring/ThreadDeadLockTest.java)

### CompletableFuture
- thenAccept() / thenAcceptAsync()  [CompletableFutureExample.java](src/main/java/com/example/CompletableFutureExample.java)
  - thenAccept：线程切换开销，**极易阻塞核心线程**
  - thenAcceptAsync：使用自定义线程池发生线程切换，极小开销
  - 不带Async后缀：回调逻辑在「完成当前 Future 的线程」执行；
  - 带Async后缀：回调逻辑在「指定线程池 / 默认线程池」执行；
    ```text
    ================= thenAccept ====================
    Future的线程：thread-1
    thenAccept回调执行线程：thread-1
    消费结果：Done!
    =============== thenAcceptAsync =================
    Future的线程：pool-1-thread-1
    Future的回调线程：pool-1-thread-2
    result：Done!
    ```
- handle / handleAsync
  - 结果 + 异常通吃，有返回值，必处理异常
- thenAcceptAsync vs thenApplyAsync
  - thenAcceptAsync 无返回值
  - thenApplyAsync 有返回值
- thenComposeAsync vs thenApplyAsync [CompletableFutureThenComposeExample.java](src/main/java/com/example/CompletableFutureThenComposeExample.java)
  - thenApplyAsync 嵌套 CompletableFuture<CompletableFuture<String>>，异步消费结果（回调套回调 → 回调地狱）
  - thenComposeAsync → 得到单层 CompletableFuture<String>
    ```text
    线程【pool-1-thread-1】执行：提交作业成功
    线程：pool-1-thread-2 → 拿到JobID：job_123456，开始执行异步任务2
    线程【pool-1-thread-1】执行：查询作业状态
    线程：pool-1-thread-2 → 最终结果：作业状态 = RUNNING
    ========================================
    线程【pool-2-thread-1】执行：提交作业成功
    线程：pool-2-thread-1 → 拿到JobID：job_123456，开始执行异步任务2
    线程【pool-2-thread-2】执行：查询作业状态
    线程：pool-2-thread-2 → 最终结果：作业状态 = RUNNING
    ```

## Java 安全模型介绍
### [PolicyFiles](https://docs.oracle.com/javase/7/docs/technotes/guides/security/PolicyFiles.html#Examples)

给classes下的文件赋读写权限

    grant codeBase "file:/Users/juntaozhang/GitProj/my-jdk-test/jaas/demo3/classes/*" {
        permission "java.io.FilePermission" "/Users/juntaozhang/GitProj/my-jdk-test/jaas/demo3/data/test.txt", "read,write";
    };
    
给classes下的文件赋所有权限

    grant codeBase "file:/Users/juntaozhang/GitProj/my-jdk-test/jaas/demo3/classes/*" {
        permission java.security.AllPermission;
    };

### [AccessController PrivilegedAction](https://www.ibm.com/developerworks/cn/java/j-lo-javasecurity/)
获取权限<br/>
  1.通过上述的policy <br/>
  2.通过调用AccessController（前提：该class必须在policy范围内） 
  
    AccessController.doPrivileged(new PrivilegedAction<String>() {
      @Override
      public String run() {
        makeFile(fileName);
        return null;
      }
    });

## 命令

### jar

```
$ javac Hello.java 
$ jar -cf hi.jar Hello.class 
$ jar -xf hi.jar
$ jar -uf hi.jar Hello.class 
$ jar -uf a.jar com/a.class    //更新文件到jar中，名录一定要对应
$ jar -uf a.jar com org
```

## 其他
* [Java基础](/java/Java基础.md)
* [JVM编译优化](/java/JVM编译优化.md)
* [JSSE](/java/JSSE-SSL.md)
* [SSL-TLS.md](/java/其他/SSL-TLS.md)
* [线程上下文类加载器](/java/线程上下文类加载器.md)
* [happens-before规则](/java/happens-before规则.md)
* [Java-volatile-怎么保证不被指令重排序优化](/java/Java-volatile-怎么保证不被指令重排序优化.md)
* [NIO](/java/NIO.md)
* [ThreadLocal源码分析](/java/ThreadLocal源码分析.md)
