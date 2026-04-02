package test.org.example.java.pattern.proxy;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class MyProxyTest {
	@Test
	public void testMyProxy() {
		InvocationHandler ih = new MyInvocationHandler();
		Person p = (Person) Proxy.newProxyInstance(Person.class.getClassLoader(), new Class[] { Person.class }, ih);
		p.walk();
		p.speak("hello");
	}
}