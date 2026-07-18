mod test_macro;
mod test_async;

use pyo3::prelude::*;

/// 一个简单的加法函数
#[pyfunction]
fn add(a: i32, b: i32) -> i32 {
    a + b
}

/// 计算斐波那契数列
#[pyfunction]
fn fibonacci(n: u32) -> u64 {
    match n {
        0 => 0,
        1 => 1,
        _ => fibonacci(n - 1) + fibonacci(n - 2),
    }
}

/// 处理字符串，返回大写版本
#[pyfunction]
fn process_string(s: &str) -> String {
    s.to_uppercase()
}

/// 返回一个简单的问候语
#[pyfunction]
fn hello(name: &str) -> String {
    format!("Hello, {}!", name)
}

/// 创建一个包含多个函数的模块
#[pymodule]
fn hello_pyrust(m: &Bound<'_, PyModule>) -> PyResult<()> {
    m.add_function(wrap_pyfunction!(add, m)?)?;
    m.add_function(wrap_pyfunction!(fibonacci, m)?)?;
    m.add_function(wrap_pyfunction!(process_string, m)?)?;
    m.add_function(wrap_pyfunction!(hello, m)?)?;
    Ok(())
}