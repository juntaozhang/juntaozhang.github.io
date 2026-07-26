// 导入过程宏需要的库
use proc_macro::TokenStream;
use quote::quote;
use syn::{parse_macro_input, FnArg, ItemFn, Pat, Data, DeriveInput, Fields};


// 定义一个过程宏：#[hello_macro]
#[proc_macro]
pub fn hi_macro(_input: TokenStream) -> TokenStream {
    // 这里是 Rust 代码生成器
    let generated_code = quote! {
        // 宏展开后，会在这里插入代码
        println!("hello macro！");
    };

    // 返回生成的代码
    generated_code.into()
}


#[proc_macro_attribute]
pub fn log(_attr: TokenStream, item: TokenStream) -> TokenStream {
    // 1. 将 TokenStream 解析为语法树 (AST)
    let input = parse_macro_input!(item as ItemFn);

    // 提取函数的各个部分
    let vis = &input.vis;
    let sig = &input.sig;
    let block = &input.block;
    let fn_name = &sig.ident;

    // 2. 提取函数参数名，用于打印入参日志
    let args_logging: Vec<_> = sig.inputs.iter().filter_map(|arg| {
        if let FnArg::Typed(pat_type) = arg {
            if let Pat::Ident(pat_ident) = &*pat_type.pat {
                let arg_name = &pat_ident.ident;
                // 生成类似 println!("参数 xxx = {:?}", xxx); 的代码
                Some(quote! {
                    println!("[LOG] 参数 {} = {:?}", stringify!(#arg_name), #arg_name);
                })
            } else {
                None
            }
        } else {
            None
        }
    }).collect();

    // 3. 使用 quote! 宏生成带有 AOP 日志逻辑的新函数代码
    let expanded = quote! {
        #vis #sig {
            // 函数执行前的日志（打印入参）
            println!("[LOG] 开始调用函数: {}", stringify!(#fn_name));
            #(#args_logging)*

            // 执行原有的函数体
            let result = (|| #block)();

            // 函数执行后的日志（打印返回值）
            println!("[LOG] 函数 {} 执行完毕，返回值 = {:?}", stringify!(#fn_name), result);

            // 返回原函数的结果
            result
        }
    };

    // 4. 将生成的代码转换回 TokenStream 交给编译器
    TokenStream::from(expanded)
}

#[proc_macro_derive(ToQueryParams)]
pub fn derive_to_query_params(input: TokenStream) -> TokenStream {
    // 1. 解析输入的 TokenStream，拿到结构体的语法树
    let input = parse_macro_input!(input as DeriveInput);

    // 提取结构体的名称
    let name = input.ident;

    // 2. 提取结构体的字段（只处理带命名字段的结构体）
    let fields = match input.data {
        Data::Struct(data) => match data.fields {
            Fields::Named(fields) => fields.named,
            _ => panic!("ToQueryParams 只支持带命名字段的结构体"),
        },
        _ => panic!("ToQueryParams 只能用于结构体"),
    };

    // 3. 遍历每个字段，生成对应的参数提取代码
    let field_conversions = fields.iter().map(|field| {
        let field_name = field.ident.as_ref().unwrap(); // 获取字段名，如 name, age
        let field_name_str = field_name.to_string();   // 转为字符串，如 "name", "age"

        // 生成类似 if let Some(ref value) = self.name { params.push(("name", value.to_string())); } 的代码
        quote! {
            if let Some(ref value) = self.#field_name {
                params.push((#field_name_str.to_string(), value.to_string()));
            }
        }
    });

    // 4. 使用 quote! 宏拼装最终的 impl 代码块
    let expanded = quote! {
        impl #name {
            pub fn to_query_params(&self) -> Vec<(String, String)> {
                let mut params = Vec::new();
                // 将上面生成的所有字段的转换代码插入到这里
                #(#field_conversions)*
                params
            }
        }
    };

    // 5. 将生成的代码转换回 TokenStream 交给编译器
    TokenStream::from(expanded)
}