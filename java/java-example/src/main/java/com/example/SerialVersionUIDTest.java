package com.example;

import java.io.*;

public class SerialVersionUIDTest {
//    static class User implements Serializable {
//        @Serial
//        private static final long serialVersionUID = 1L;
//
//        private String age;
//
//        User(String age) {
//            this.age = age;
//        }
//
//        @Override
//        public String toString() {
//            return "User{age='" + age + "'}";
//        }
//    }

    static class User implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer age;
        private String name;

        public User(Integer age, String name) {
            this.age = age;
            this.name = name;
        }

//        private void writeObject(ObjectOutputStream out) throws IOException {
//            out.writeInt(age);
//            out.writeUTF(name);
//        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            // 必须注意顺序
            Object obj = in.readObject();
            if (obj instanceof String) {
                this.age = Integer.parseInt((String) obj);
            } else if (obj instanceof Integer) {
                this.age = (Integer) obj;
            }
            try {
                this.name = (String) in.readObject();
            } catch (EOFException e) {
                this.name = "Anonymous";
            }
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + "}";
        }
    }

    public static void main(String[] args) throws Exception {
        String fileName = "user_v1.ser";

//        System.out.println("=== 1. 开始序列化 (使用 V1 类) ===");
//         User user = new User("38");
////        User user = new User(38, "zhangsan");
//        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
//            out.writeObject(user);
//            System.out.println(">> 文件生成成功：user.ser");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println("\n=== 2. 开始反序列化 ===");
        // 删除、新增 字段都没有关系，但是改变原有字段的类型会报错
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            User userV2 = (User) in.readObject();
            System.out.println(">> 反序列化成功！");
            System.out.println(">> 对象详情: " + userV2);
        }
    }
}
