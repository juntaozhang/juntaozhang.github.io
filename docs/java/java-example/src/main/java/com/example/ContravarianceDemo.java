package com.example;

import java.util.function.Function;

/**
 * java 逆变
 */
public class ContravarianceDemo {
    public static class Animal {
        public String getSound() {
            return "动物叫声";
        }
    }

    public static class Cat extends Animal {
        @Override
        public String getSound() {
            return "喵喵喵";
        }
    }

    public static void feedCat(Function<? super Cat, String> processor) {
        Cat myCat = new Cat();
        String result = processor.apply(myCat);
        System.out.println("结果: " + result);
    }

    public static void main(String[] args) {
        feedCat((cat) -> "喂猫: " + cat.getSound());
        feedCat((animal) -> "喂动物: " + animal.getSound());
        feedCat((obj) -> "喂对象: " + obj.toString());
    }
}
