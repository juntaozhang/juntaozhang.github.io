<summary>AOP</summary>
<details>

不修改业务代码，统一添加通用功能（日志、事务、权限）。
```java
@Aspect
@Component
public class LogAspect {
    @Before("execution(* com.xxx.service.*.*(..))")
    public void before() {
        System.out.println("方法执行前打印日志");
    }
}
```
</details>
<br/>
<summary>IOC</summary>
<details>

把创建对象、管理对象的权力交给 Spring 容器，不用自己 new。

```java
@Service
public class UserService {} 

// 直接用，Spring 已经创建好
@Autowired
UserService userService; 
```
</details>
<br/>
<summary>容器管对象 IOC 好处是？</summary>
<details>

- 解耦合、管单例、自动注入、好扩展
- 多个实现通过 @Qualifier 指定
</details>
<br/>
<summary>IOC 为什么默认单例？多例有什么问题？</summary>
<details>

- 全局只创建一个对象，不反复 new，不浪费资源。
- Spring Bean 默认不存成员变量数据，单例完全够用。
- 多例（Prototype）有什么问题？
  - 多例：费内存、性能差、Spring 不管理销毁
</details>
<br/>
<summary>Spring 如何解决循环依赖？关闭三级缓存会怎样？</summary>
<details>

- 三级缓存结构
  - 一级缓存：成品单例 Bean
  - 二级缓存：半成品 Bean（已实例化，未填充属性）
  - 三级缓存：Bean 工厂 Lambda，提前暴露引用
- 什么是循环依赖?
    ```text
    A → B
    B → A
  
    A 先创建 → 放三级缓存
    A 找 B → B 创建
    B 找 A → 从三级拿到 A → 移入二级缓存
    B 创建完成 → 入一级缓存
    A 拿到 B → 创建完成 → 入一级缓存
    ```
- 为什么必须用三级？二级不行吗？

  关键：因为 AOP 代理！ 如果没有 AOP，二级缓存就能解决。有 AOP 时，需要三级缓存的工厂来生成代理对象
    ```text
    场景：A 有 AOP 代理（事务 / 日志），A 依赖 B，B 依赖 A
    
    A 实例化 → 原始对象 A
    把工厂 () -> 生成A的代理 放入三级缓存
  
    A 依赖 B → 去创建 B
    B 没有 AOP，把工厂 "() -> 返回B的原始对象" 放入三级缓存
  
    B 依赖 A → 从三级缓存拿工厂，执行工厂 → 生成代理 A_proxy
    把 A_proxy 放入二级缓存
    B 注入 A_proxy
    B 创建完成 放入一级缓存
  
    A 继续初始化 → 发现代理已经提前创建好了，直接用
    A 最终成品 = A_proxy
    A_proxy 进入一级缓存
    ```
- 什么情况解决不了？
  - 构造器注入的循环依赖，因为实例化都做不到，根本没法放进缓存！

```text

Spring 通过提前曝光半成品 Bean，解决单例、setter 注入循环依赖。
关闭三级缓存后果
    setter 循环依赖直接报错
    构造器循环依赖本来就无法解决
    AOP 动态代理循环依赖全部失效
    Spring 只能报错 BeanCurrentlyInCreationException
```
</details>
<br/>

<summary>Spring Boot 完整启动流程</summary>
<details>

- 加载主启动类，解析 @SpringBootApplication
  - SpringBootConfiguration: 标记当前类为配置类，Spring 会把它当做 Bean 配置解析。
  - EnableAutoConfiguration: 开启自动配置，读取 META-INF 下自动配置文件，按条件批量注入 Bean。
  - ComponentScan: 自动扫描当前包及其子包所有 @Component/@Service/@Controller，注册到 IOC 容器。
- 创建 SpringApplication 实例，加载所有配置、监听器、初始化器
- 运行 run () 方法，触发环境准备、打印 banner
- 创建 Spring IOC 容器（ApplicationContext）
- 执行自动配置，扫描 Bean、注册 Bean、解决循环依赖
- 完成 Bean 生命周期、AOP 动态代理
- 初始化内嵌 Tomcat、Jetty 容器
- 启动 Web 服务，监听端口，接受请求
</details>
<br/>
<summary>自动配置类是怎么被加载进来的？</summary>
<details>

```text
@EnableAutoConfiguration 通过 @Import 导入选择器
选择器去读取 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
获取所有预设自动配置全限定类名
通过条件注解过滤：
    有没有对应依赖 @ConditionalOnClass
    容器有没有同类型 Bean @ConditionalOnMissingBean
    配置文件是否匹配 @ConditionalOnProperty
满足条件才加载配置类，创建 Bean 放入容器
```
</details>
<br/>
<summary>自定义 Starter 原理？手写流程</summary>
<details>

```text
标准自定义 Starter 两步
autoconfigure 自动配置模块
写配置类 + 条件注解 @ConditionalOnClass
写属性绑定 @ConfigurationProperties
编写 META-INF 自动配置文件，声明配置类
starter 启动器模块
只引入依赖，不写代码，聚合 autoconfigure + 业务依赖
原理
别人引入你的 starter 依赖 → Spring Boot 扫描到自动配置文件
满足条件就自动装配 Bean，使用者直接注入使用，零配置。
```
</details>
<br/>
<summary>接口限流、日志、鉴权如何实现？</summary>
<details>

- 接口日志：AOP 环绕通知 → 记录入参、出参、耗时
- 接口鉴权：拦截器 / Filter → 校验 token
- 接口限流：Redis + Lua / 拦截器 → 控制访问频率 
  - [rate-limiter.md](../algorithm/src/test/java/cn/juntaozhang/design/rate-limiter.md)
</details>
<br/>
<summary>Spring Boot 优雅停机？ 优雅停机都做了哪些事？</summary>
<details>

配置 graceful， 优雅停机 = 不接收新请求 + 等正在执行的请求跑完 + 安全释放资源 + 最后关闭服务器。
- 关闭 @Service / @Controller，不接受新API
- 关闭线程池
- 释放数据库连接
- 关闭定时任务
- 关闭嵌入式服务器
</details>
<br/>
<summary>接口幂等性如何实现？</summary>
<details>

用唯一标识做标记，执行前查标记，执行后写标记。\
第一次执行：标记不存在 → 放行 → 写入标记。\
第二次执行：标记已存在 → 直接返回结果 → 拒绝执行。
</details>
<br/>
<summary></summary>
<details>
<br/>




# 一、基础必问（100% 会被问到）
1. **Spring Boot 的核心优点是什么？**
4. **Starter 是什么？原理？**
5. **Spring Boot 配置文件加载优先级？**
   application.properties / application.yml / 外部配置
6. **`@ConfigurationProperties` 和 `@Value` 区别？**
7. **Spring Boot 如何实现热部署？**

---

# 二、核心原理（中高级必问）
8. **Spring Boot 启动流程（超级高频）**
9. **Spring Boot 内嵌 Tomcat 原理？如何替换成 Jetty/Undertow？**
10. **自动配置如何生效、如何禁用、如何自定义？**
11. **`@Conditional` 系列注解作用？**
    OnClass、OnBean、OnMissingBean、OnProperty
12. **Spring Boot 生命周期回调有哪些？**
    CommandLineRunner、ApplicationRunner、@PostConstruct、InitializingBean
13. **Spring Boot 事务如何使用？原理？**
14. **Spring Boot 如何实现跨域（CORS）？**

---

# 三、高级深度题（阿里/腾讯/字节最爱问）
16. **Spring Boot 中的 Bean 是线程安全的吗？为什么？**
17. **Spring Boot 自动配置与 Spring 原生配置区别？**
19. **Spring Boot 如何自定义 Starter？（手写 Starter 高频）**
20. **Spring Boot 启动特别慢怎么排查？**
21. **Spring Boot 如何做权限控制（Shiro / Security）？**
22. **Spring Boot 如何集成 MyBatis-Plus / JPA？原理？**

---

</details>
