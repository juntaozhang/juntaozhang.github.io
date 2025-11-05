package cn.juntaozhang.jdk;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class JavaSerializationTest {
    public static void main(String[] args) throws IOException {
        System.out.println("=== Java序列化重定位问题演示 ===");

        // 创建测试对象
        Person person1 = new Person("Alice", 25);
        Person person2 = new Person("Bob", 30);

        // 步骤1: 正常序列化两个对象到同一个流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        System.out.println("写入对象: " + person1);
        oos.writeObject(person1);
        oos.flush();

        // 记录第一个对象结束位置
        int position1 = baos.toByteArray().length;
        System.out.println("第一个对象序列化后字节数: " + position1);

        System.out.println("写入对象: " + person2);
        oos.writeObject(person2);
        oos.flush();

        // 记录第二个对象结束位置
        int position2 = baos.toByteArray().length;
        System.out.println("两个对象序列化后总字节数: " + position2);

        oos.close();

        // 获取完整的序列化数据
        byte[] completeData = baos.toByteArray();

        // 提取每个对象的字节块
        byte[] obj1Bytes = Arrays.copyOfRange(completeData, 0, position1);
        byte[] obj2Bytes = Arrays.copyOfRange(completeData, position1, position2);

        System.out.println("第一个对象字节长度: " + obj1Bytes.length);
        System.out.println("第二个对象字节长度: " + obj2Bytes.length);

        // 步骤2: 测试正常顺序反序列化
        System.out.println("\n--- 测试正常顺序反序列化 ---");
        testDeserialization("正常顺序", completeData);

        // 步骤3: 重新排列字节块 (obj2在前, obj1在后)
        System.out.println("\n--- 测试重新排列后反序列化 ---");
        byte[] reorderedData = new byte[completeData.length];
        System.arraycopy(obj2Bytes, 0, reorderedData, 0, obj2Bytes.length);
        System.arraycopy(obj1Bytes, 0, reorderedData, obj2Bytes.length, obj1Bytes.length);

        testDeserialization("重新排列", reorderedData);

        // 步骤4: 分析字节内容
        System.out.println("\n--- 字节内容分析 ---");
        analyzeBytes(obj1Bytes, "第一个对象");
        analyzeBytes(obj2Bytes, "第二个对象");
    }

    private static void testDeserialization(String description, byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);

            Object first = ois.readObject();
            Object second = ois.readObject();

            System.out.println(description + " - 成功读取:");
            System.out.println("  第一个对象: " + first);
            System.out.println("  第二个对象: " + second);

            ois.close();
        } catch (Exception e) {
            System.out.println(description + " - 反序列化失败:");
            System.out.println("  错误: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void analyzeBytes(byte[] bytes, String description) {
        System.out.println(description + " 字节分析:");

        // 显示前20个字节的十六进制表示
        System.out.print("  前20个字节: ");
        for (int i = 0; i < Math.min(20, bytes.length); i++) {
            System.out.printf("%02X ", bytes[i]);
        }
        System.out.println();

        // 检查是否包含Java序列化的魔法数字
        if (bytes.length >= 4) {
            boolean hasMagic = (bytes[0] == (byte) 0xAC && bytes[1] == (byte) 0xED);
            System.out.println("  包含Java序列化魔法数字: " + hasMagic);
        }

        // 粗略检查是否包含类名信息
        String bytesAsString = new String(bytes);
        boolean hasClassName = bytesAsString.contains("Person");
        System.out.println("  包含类名信息: " + hasClassName);

        System.out.println();
    }

    // Person类 - 用于测试
    public static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }
    }
}
