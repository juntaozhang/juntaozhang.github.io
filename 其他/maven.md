## maven center
```
<mirrors>
  <mirror>
    <id>ali-maven</id>
    <name>aliyun maven</name>
　　<url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```




## maven jetty debug ideal
```
Runner VM
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=19000
Parameters Command Line 
jetty:run
```
debug
```
Remote Java
```


## 顶级pom和子pom版本批量修改

```
修改版本
mvn versions:set -DnewVersion=xxx
回滚版本，提交后不能回滚
mvn versions:revert
提交版本变更
mvn versions:commit
```
