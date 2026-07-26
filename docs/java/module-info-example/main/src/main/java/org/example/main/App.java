package org.example.main;

import org.example.Util;

import java.lang.reflect.Method;


public class App {
    public static void main(String[] args) throws Exception {
        Util.hi();

        // Package 'org.b' is declared in module 'lib.b', which does not export it to module 'main'
        // System.out.println(new org.b.Person("zhangsan", 38));

        // --add-opens lib.b/org.b=main
        Class<?> clazz = Class.forName("org.b.Person");
        Method method = clazz.getDeclaredMethod("hi");
        method.setAccessible(true);
        method.invoke(null);
    }
}
