###maven center
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




#####maven jetty debug ideal
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
