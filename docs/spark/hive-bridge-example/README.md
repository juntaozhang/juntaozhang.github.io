# Java ClassLoader Isolation Example Project

## Project Overview

This project demonstrates **Java ClassLoader isolation technology**, which is crucial for resolving dependency conflicts and version compatibility issues in Java applications. This technology is particularly important in big data ecosystems, especially when components like Spark and Hive need to coexist with different versions of third-party libraries.

## Project Structure

```
hive-bridge-example/
├── utils-v1/           # Utility library version 1.0.0
├── utils-v2/           # Utility library version 2.0.0  
└── utils-example/      # Demonstrates how to use ClassLoader isolation technology
```

### Module Description

#### utils-v1 (Version 1.0.0)
- **Function**: Provides `MyTest.getVersion()` method returning "Version from 1.0.0"
- **Purpose**: Simulates legacy version of utility library

#### utils-v2 (Version 2.0.0)  
- **Function**: Provides `MyTest.getVersion()` method returning "Version from 2.0.0"
- **Purpose**: Simulates new version of utility library

#### utils-example (Demo Module)
- **Function**: Demonstrates three different ClassLoader usage patterns

## Technical Implementation
### [Example1.java](utils-example/src/main/java/org/example/Example1.java): Basic URLClassLoader Usage
### [Example2.java](utils-example/src/main/java/org/example/Example2.java): Basic URLClassLoader Usage with parent ClassLoader
#### Parent = Example2.class.getClassLoader(), jdk.internal.loader.ClassLoaders$AppClassLoader
> Version from Main

双亲委托机制中，Example2 的 ClassLoader（AppClassLoader）定义了 `org.example.MyTest`，所以不会使用 utils-v1 或 utils-v2 中的 `org.example.MyTest`。

#### Parent = Example2.class.getClassLoader().getParent() jdk.internal.loader.ClassLoaders$PlatformClassLoader
> Version from 2.0.0

双亲委托机制中，PlatformClassLoader 中并没有定义 `org.example.MyTest`，所以直接使用了 utils-v2 中的 `org.example.MyTest`。

#### Parent = null
> Version from 2.0.0

没有双亲委托机制，直接使用了 utils-v2 中的 `org.example.MyTest`。


### [Example3.java](utils-example/src/main/java/org/example/Example3.java): Custom ClassLoader Implementation
