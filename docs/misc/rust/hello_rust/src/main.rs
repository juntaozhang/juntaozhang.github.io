// fn main() {
//     println!("Hello, world!");
//     {
//         // str 非定长的
//         // &str 定长的胖指针
//         let s1: &str = "hello"; // 静态内存, 非栈 非堆, 类似Java 在常量池
//         let s2: &str = "rust"; // s1,s2 类型相同
//
//         // 类型不相同，数组长度是编译期就确定的
//         let arr1 = [1, 2];
//         let arr2 = [1, 2, 3];
//     }
// }



#[derive(Debug)]
struct Rectangle {
    width: u32,
    height: u32,
}
impl Rectangle {
    fn area(&self) -> u32 {
        self.width * self.height
    }
}

fn main() {
    {
        let rect1 = Rectangle {
            width: 30,
            height: 50,
        };

        println!("rect1 is {rect1:?}");
        println!("rect1 is {rect1:#?}");
    }


    {
        let scale = 2;
        let rect1 = Rectangle {
            width: dbg!(30 * scale),
            height: 50,
        };

        dbg!(&rect1);
    }

    {
        let rect1 = Rectangle {
            width: 30,
            height: 50,
        };

        println!(
            "The area of the rectangle is {} square pixels.",
            rect1.area()
        );
    }
}