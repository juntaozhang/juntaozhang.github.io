

## Python call Rust

> python -m venv .venv
> . ./.venv/bin/activate


### maturin
Rust 编译成 Python 模块的官方推荐工具
作用：一键编译 + 安装到 Python，完全不用处理文件

> pip install maturin
> maturin develop --release
> python test_hello.py


## 基本语法

```rust
fn main() {
    // str 非定长的
    // &str 定长的胖指针
    let s1: &str = "hello"; // 静态内存, 非栈 非堆, 类似Java 在常量池
    let s2: &str = "rust"; // s1,s2 类型相同

    // 类型不相同，数组长度是编译期就确定的
    let arr1 = [1, 2];
    let arr2 = [1, 2, 3];
}
```

- https://rust-lang.org/zh-CN/learn/
- https://kaisery.github.io/trpl-zh-cn/title-page.html
- https://nomicon.purewhite.io/borrow-splitting.html



