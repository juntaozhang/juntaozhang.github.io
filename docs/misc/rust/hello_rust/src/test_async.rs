use tokio::time::{sleep, Duration};

// 1. 用 async 定义一个异步函数，它返回一个匿名的 Future
async fn fetch_data(task_name: &str, wait_secs: u64) -> String {
    println!("任务 [{}] 开始...", task_name);

    // 2. sleep 返回一个 Future，.await 会暂停当前任务，把 CPU 让给其他任务
    // 这里模拟一个耗时的 I/O 操作（比如查数据库、发网络请求）
    sleep(Duration::from_secs(wait_secs)).await;

    println!("任务 [{}] 完成！", task_name);
    format!("任务 [{}] 的结果", task_name)
}

#[tokio::main] // 3. 这个宏把 main 函数变成一个 Tokio 运行时的入口
async fn main() {
    println!("主程序开始");

    // 4. 调用异步函数会立刻返回一个 Future（此时任务还没开始跑）
    let f1 = fetch_data("A", 2);
    let f2 = fetch_data("B", 1);

    // 5. 使用 join! 宏并发地等待两个 Future 完成
    // 任务 B 等待时间短，会先打印“完成”，然后才是任务 A
    let (res1, res2) = tokio::join!(f1, f2);

    println!("获取到的结果: {}, {}", res1, res2);
    println!("主程序结束");
}