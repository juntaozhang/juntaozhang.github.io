package org.example;

import java.net.URL;
import java.net.URLClassLoader;

public class Example1 {
    Class<?> classV1;
    Class<?> classV2;

    public Example1() throws Exception {
        URL[] libraryV1Urls = {new URL("jar:file:/Users/juntzhang/.m2/repository/org/example/utils/1.0.0/utils-1.0.0.jar!/")};
        URL[] libraryV2Urls = {new URL("jar:file:/Users/juntzhang/.m2/repository/org/example/utils/2.0.0/utils-2.0.0.jar!/")};
        try (URLClassLoader classLoaderV1 = new URLClassLoader(libraryV1Urls, null);
             URLClassLoader classLoaderV2 = new URLClassLoader(libraryV2Urls, null)) {
            classV1 = classLoaderV1.loadClass("org.example.MyTest");
            classV2 = classLoaderV2.loadClass("org.example.MyTest");
        }
    }

    public static void main(String[] args) throws Exception {
        Example1 main = new Example1();
        System.out.println(main.getVersion1());
        System.out.println(main.getVersion2());
    }

    public String getVersion1() throws Exception {
        return (String) classV1.getMethod("getVersion").invoke(null);
    }

    public String getVersion2() throws Exception {
        return (String) classV2.getMethod("getVersion").invoke(null);
    }
}