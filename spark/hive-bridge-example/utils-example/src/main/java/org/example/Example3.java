package org.example;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Example3 {
    public static void main(String[] args) throws Exception {
        final String name = "org.example.MyTest";
        ClassLoader classLoaderV1 = new ClassLoader(null) {
            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                try {
                    try (JarFile jarFile = new JarFile("/Users/juntzhang/.m2/repository/org/example/utils/1.0.0/utils-1.0.0.jar")) {
                        String classPath = name.replace('.', '/') + ".class";
                        JarEntry entry = jarFile.getJarEntry(classPath);
                        InputStream inputStream = jarFile.getInputStream(entry);
                        byte[] classBytes = inputStream.readAllBytes();
                        inputStream.close();
                        return defineClass(name, classBytes, 0, classBytes.length);
                    }
                } catch (Exception e) {
                    throw new ClassNotFoundException("Failed to load class " + name, e);
                }
            }
        };
        Class<?> clazz = classLoaderV1.loadClass(name);
        System.out.println((String) clazz.getMethod("getVersion").invoke(null));


    }
}
