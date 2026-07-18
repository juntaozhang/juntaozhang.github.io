// 导入宏
use hello_macro::{hi_macro, log, ToQueryParams};

// 类属性宏
#[log]
pub fn add(a: i32, b: i32) -> i32 {
    a + b
}

// 类函数宏
pub fn hi() {
    hi_macro!();
}

// 自定义 derive 宏
#[derive(ToQueryParams)]
struct UserFilter {
    name: Option<String>,
    age: Option<u32>,
    active: Option<bool>,
}
pub fn test_derive() -> bool {
    let filter = UserFilter {
        name: Some("Alice".to_string()),
        age: None,
        active: Some(true),
    };
    let params = filter.to_query_params(); // here is vec
    println!("{:?}", params);
    true
}

fn main() {
    add(10, 20);
    hi();
    test_derive();
}
