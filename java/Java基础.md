# Java基础
### [byte[], int, long三者之间的相互转换代码](https://gitee.com/zjt_hans/hello-java/JavaSE/src/test/java/test/org/example/java/base/LongIntegerBytesConvertor.java)

### 基本数据类型
```
bit：位 
	一个二进制数据0或1，是1bit；
byte：字节
    存储空间的基本计量单位，如：MySQL中定义 VARCHAR(45) 即是指 45个字节；
    1 byte = 8 bit
一个英文字符占一个字节；
    1 字母 = 1 byte = 8 bit
一个汉字占2个字节；
    1 汉字 = 2 byte = 16 bit
	byte：一个字节（8位）（-128~127）（-2的7次方到2的7次方-1）
	short：两个字节（16位）（-32768~32767）（-2的15次方到2的15次方-1）
	int：四个字节（32位）（一个字长）（-2147483648~2147483647）（-2的31次方到2的31次方-1）
	long：八个字节（64位）（-9223372036854774808~9223372036854774807）（-2的63次方到2的63次方-1）
	float：四个字节（32位）（3.402823e+38 ~ 1.401298e-45）（e+38是乘以10的38次方，e-45是乘以10的负45次方）
	double：八个字节（64位）（1.797693e+308~ 4.9000000e-324）
```

### java >>>(补零) >>(补符号位) <<
[ShiftingTest](https://gitee.com/zjt_hans/hello-java/JavaSE/src/test/java/test/org/example/java/ShiftingTest.java)

### & |
[AndOrTest](https://gitee.com/zjt_hans/hello-java/JavaSE/src/main/java/test/test/org/example/java/AndOrTest.java)

### 6.编码
    0x 16进制
    0  8进制
    java默认文件编码是UTF-16BE
    file.encoding 操作系统默认编码


### java访问控制权限
```
        public    protected     default    private
同一个类     √            √          √          √
同一个包     √            √          √
子类        √            √
不同包       √
```