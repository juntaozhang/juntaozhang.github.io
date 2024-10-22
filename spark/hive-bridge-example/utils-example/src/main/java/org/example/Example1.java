package org.example;

import java.net.URL;
import java.net.URLClassLoader;

public class Example1 {
    public static void main(String[] args) throws Exception {
        URL[] libraryV1Urls = {new URL("jar:file:/Users/juntzhang/.m2/repository/org/example/utils/1.0.0/utils-1.0.0.jar!/")};
        URLClassLoader classLoaderV1 = new URLClassLoader(libraryV1Urls, null);
        Class<?> clazz = classLoaderV1.loadClass("org.example.MyTest");
        System.out.println((String) clazz.getMethod("getVersion").invoke(null));

        URL[] libraryV2Urls = {new URL("jar:file:/Users/juntzhang/.m2/repository/org/example/utils/2.0.0/utils-2.0.0.jar!/")};
        URLClassLoader classLoaderV2 = new URLClassLoader(libraryV2Urls, Example2.class.getClassLoader().getParent()); // 一定要用parent，不然当前类会覆盖libraryV1Urls
        clazz = classLoaderV2.loadClass("org.example.MyTest");
        System.out.println((String) clazz.getMethod("getVersion").invoke(null));

        // 隔离了org.example.MyTest类的两个版本
        clazz  = Example1.class.getClassLoader().loadClass("org.example.MyTest");
        System.out.println((String) clazz.getMethod("getVersion").invoke(null));
        System.out.println(MyTest.getVersion());

        System.out.println(classLoaderV1);
        System.out.println(Example2.class.getClassLoader());
        System.out.println(Example2.class.getClassLoader().getParent());

    }
}
