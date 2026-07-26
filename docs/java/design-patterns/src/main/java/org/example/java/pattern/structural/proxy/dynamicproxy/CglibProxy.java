package org.example.java.pattern.structural.proxy.dynamicproxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 不能对final修饰的类进行代理
 */
public class CglibProxy implements MethodInterceptor {
  private Object target;

  public Object getInstance(Object target) {
    this.target = target;
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(this.target.getClass());
    // 回调方法
    enhancer.setCallback(this);
    // 创建代理对象
    return enhancer.create();
  }

  @Override
  public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
    System.out.println("事物开始");
    Object result = proxy.invokeSuper(o, args);
    System.out.println("事物结束");
    return result;
  }
}
