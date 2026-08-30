# fastjson 1.2.68 Tests on 1.2.84 失败案例分析

## 说明

基于 `failing-tests-summary.txt`，将 fastjson 1.2.68 的测试用例在 1.2.84 上运行产生的失败按**现象大类**归并。

- 所有 `autoType` / `@type` 相关的安全限制统一归为一类，不细分具体类名。
- 每类均给出原始错误示例，并附明确 RCA（含代码场景）。
- 原始运行结果：`Tests run: 4824, Failures: 8, Errors: 11, Skipped: 0`，合计 **19** 个失败/错误方法。

---

## 一、autoType / @type 安全策略限制

**统一特征**：

```text
com.alibaba.fastjson.JSONException: autoType is not support. xxx
```

1.2.84 默认关闭 autoType，所有依赖 `@type` 反序列化到具体类的测试都被拦截。

**涉及用例**：`Issue1240`、`AutoTypeTest7`、`DefaultOAuth2AccessTokenTest`、`DefaultSavedRequestTest`

**数量**：8 个 error

### 示例 1：Spring Util — `LinkedMultiValueMap`

```text
=== com.alibaba.json.bvt.issue_1200.Issue1240.txt ===
test_for_issue(com.alibaba.json.bvt.issue_1200.Issue1240)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. org.springframework.util.LinkedMultiValueMap
```

**RCA**：代码调用 `JSON.parseObject(..., Object.class)` 解析带 `@type:org.springframework.util.LinkedMultiValueMap` 的 JSON。1.2.84 默认安全策略不再允许未在白名单中的类被 autoType 反序列化，直接抛 `JSONException`。

### 示例 2：Spring Security — `SecurityContextImpl`

```text
=== com.alibaba.json.bvt.support.spring.security.DefaultSavedRequestTest.txt ===
test_SecurityContextImpl(com.alibaba.json.bvt.support.spring.security.DefaultSavedRequestTest)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. org.springframework.security.core.context.SecurityContextImpl
```

**RCA**：序列化后的 Spring Security 对象 JSON 中携带 `@type`，反序列化时目标类 `SecurityContextImpl` 不在 1.2.84 的默认 autoType 白名单中。

### 示例 3：Spring Cache / OAuth2

```text
=== com.alibaba.json.bvt.parser.autoType.AutoTypeTest7.txt ===
test_2(com.alibaba.json.bvt.parser.autoType.AutoTypeTest7)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. org.springframework.cache.support.NullValue

=== com.alibaba.json.bvt.support.spring.security.DefaultOAuth2AccessTokenTest.txt ===
test_0(com.alibaba.json.bvt.support.spring.security.DefaultOAuth2AccessTokenTest)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
```

**RCA**：`NullValue`、`DefaultOAuth2AccessToken` 等第三方类未被 1.2.84 默认放行。`AutoTypeTest7.test_3` 进一步因为 `CannotAcquireLockException` 无法反序列化为具体异常类型，退化为 `JSONObject`，导致后续强转 `Exception` 时产生 `ClassCastException`。

---

## 二、序列化输出格式变化

**统一特征**：同样的输入，1.2.84 输出的 JSON 字符串格式变了，导致 `assertEquals` 失败。包括日期分隔符、集合类型标记。

**涉及用例**：`Issue1493`、`WriteClassNameTest_Set4`、`WriteClassNameTest_Set`

**数量**：3 个 failure

### 示例 1：`LocalDateTime` 默认格式从空格分隔变为 `T` 分隔

```text
=== com.alibaba.json.bvt.issue_1400.Issue1493.txt ===
test_for_issue(com.alibaba.json.bvt.issue_1400.Issue1493)  <<< FAILURE!
org.junit.ComparisonFailure: expected:<...,"time2":"2017-09-22[T]15:08:56"}> but was:<...,"time2":"2017-09-22[ ]15:08:56"}>
```

**RCA**：测试代码使用 `SerializerFeature.WriteDateUseDateFormat` 序列化 `java.time.LocalDateTime`。1.2.68 默认日期时间格式为 `"yyyy-MM-dd HH:mm:ss"`（空格分隔）；1.2.84 改为 ISO8601 格式 `"yyyy-MM-dd'T'HH:mm:ss"`（`T` 分隔），导致断言中期望的 `T` 与实际输出的空格不一致。

### 示例 2：`Set` 字段写类名时多输出 `[Set]` 标记

```text
=== com.alibaba.json.bvt.writeClassName.WriteClassNameTest_Set.txt ===
test_list(com.alibaba.json.bvt.writeClassName.WriteClassNameTest_Set)  <<< FAILURE!
org.junit.ComparisonFailure: expected:<...eTest_Set$A","list":[][...,...}> but was:<...eTest_Set$A","list":[Set][...,...}>
```

**RCA**：开启 `SerializerFeature.WriteClassName` 后，对 `Set` 类型字段做序列化。1.2.84 会额外输出 `[Set]` 类型标识；1.2.68 直接输出 `[]`。

---

## 三、反序列化 / 解析 / 类型转换行为变化

**统一特征**：不是 autoType 被拦截，而是反序列化逻辑、字符串解析、类型转换、空值处理等行为变化。

**涉及用例**：`Issue2387`、`JSONArrayTest2`、`ReadOnlyMapTest2`、`TypeUtilsTest`、`JSONPath_12`、`ExceptionTest`

**数量**：6 个（5 failure + 1 error）

### 示例 1：`Feature.InitStringFieldAsEmpty` 对有值字段的处理变化

```text
=== com.alibaba.json.bvt.issue_2300.Issue2387.txt ===
test_for_issue(com.alibaba.json.bvt.issue_2300.Issue2387)  <<< FAILURE!
junit.framework.ComparisonFailure: expected:<{"ddd":"[","id":"","name":"]"}> but was:<{"ddd":"[sdfsd","id":"ss","name":"hh]"}>
```

**RCA**：测试输入为 `{id:"ss",ddd:"sdfsd",name:"hh"}`，并使用 `Feature.InitStringFieldAsEmpty` 解析。1.2.68 在遇到该 Feature 时会将字符串字段强制置为空串；1.2.84 仅在字段缺失或为空时置空，字段已有非空值时保留原值。

### 示例 2：`JSON.parseArray` 对非数组字符串校验更严格

```text
=== com.alibaba.json.bvt.JSONArrayTest2.txt ===
test_0(com.alibaba.json.bvt.JSONArrayTest2)  <<< ERROR!
com.alibaba.fastjson.JSONException: syntax error, expect [, actual EOF, pos 52, fieldName null
```

**RCA**：测试末尾调用 `JSON.parseArray("//arplatform.alicdn.com/images/874/1504512065305.png")`。该字符串不是合法的 JSON 数组格式。1.2.84 解析器对数组语法的校验更严格，遇到不以 `[` 开头的输入直接抛 `JSONException`。

### 示例 3：只读 Map 的 null 字段未保留

```text
=== com.alibaba.json.bvt.parser.ReadOnlyMapTest2.txt ===
test_readOnlyNullList(com.alibaba.json.bvt.parser.ReadOnlyMapTest2)  <<< FAILURE!
java.lang.AssertionError: expected null, but was:<{a=com.alibaba.json.bvt.parser.ReadOnlyMapTest2$A@29420437}>
```

**RCA**：`Entity` 中 `values` 字段只有 getter、没有 setter，输入 JSON 为 `{"values":{"a":{}}}`。1.2.68 对无 setter 的字段不赋值，保持 `null`；1.2.84 在反序列化时会尝试创建并设置该字段值，导致 `values` 不为 `null`。

### 示例 4：整数转 `java.sql.Timestamp` 不再抛异常

```text
=== com.alibaba.json.bvt.parser.TypeUtilsTest.txt ===
test_cast_to_Timestamp_error(com.alibaba.json.bvt.parser.TypeUtilsTest)  <<< FAILURE!
java.lang.AssertionError: null
```

**RCA**：测试代码构造 `JSONObject json = {"date": 0}`，然后调用 `json.getObject("date", java.sql.Timestamp.class)`，并断言会抛出 `JSONException`。1.2.84 对整数到 `Timestamp` 的类型转换做了容错，将 `0` 解释为时间戳起点（1970-01-01 00:00:00）而不再抛异常，因此捕获的 `error` 为 `null`，`Assert.assertNotNull(error)` 失败。

### 示例 5：`JSONPath` 对含转义点号 key 的路径解析不一致

```text
=== com.alibaba.json.bvt.path.JSONPath_12.txt ===
test(com.alibaba.json.bvt.path.JSONPath_12)  <<< FAILURE!
junit.framework.AssertionFailedError: null
```

**RCA**：测试使用 `JSONPath.set` 向路径 `...['Brand\. Name']` 写入属性（key 中包含转义后的点号），随后用 `JSONPath.contains` 和 `JSONPath.eval` 读取。1.2.84 中 `JSONPath` 对含转义字符 key 的 `set`、`contains`、`eval` 三处处理不一致，导致断言失败。

### 示例 6：`Exception` 反序列化无法识别具体异常类型

```text
=== com.alibaba.json.bvt.serializer.exception.ExceptionTest.txt ===
test_exception(com.alibaba.json.bvt.serializer.exception.ExceptionTest)  <<< FAILURE!
junit.framework.AssertionFailedError: null
```

**RCA**：测试将 `IllegalAccessError` 序列化为 JSON 后再 `JSON.parse(text)`，并断言结果仍是 `IllegalAccessError`。1.2.84 序列化 `Throwable` 时默认不再写入 `@type` 或完整类名信息，反序列化后得到 `JSONObject` 或通用 `Exception`，不再保留原异常类型。

---

## 四、外部依赖版本不兼容

**统一特征**：测试代码能编译，但运行时第三方类的方法签名和当前依赖版本不匹配。

**涉及用例**：`Issue2685`

**数量**：1 个 error

### 示例

```text
=== com.alibaba.json.bvt.issue_2600.Issue2685.txt ===
test_field(com.alibaba.json.bvt.issue_2600.Issue2685)  <<< ERROR!
java.lang.NoSuchMethodError: org.apache.commons.lang3.RandomUtils.nextInt()I
	at com.zx.sms.common.util.DefaultSequenceNumberUtil.<clinit>(DefaultSequenceNumberUtil.java:85)
```

**RCA**：当前 `pom.xml` 中 `commons-lang3` 版本为 `3.4`，该版本 `RandomUtils` 没有无参 `nextInt()` 方法；`sms-core` 运行时需要的是 `commons-lang3 3.5+`。建议将 `commons-lang3` 升级到 `3.5`，与原始 1.2.68 pom 保持一致。

---

## 汇总

| 大类 | 数量 | 性质 |
| --- | ---: | --- |
| autoType / @type 安全限制 | 8 | ✅ 1.2.84 预期行为变更 |
| 序列化输出格式变化 | 3 | ✅ 大多是预期行为变更 |
| 反序列化 / 解析 / 类型转换行为变化 | 6 | ⚠️ 需单独确认是 bug 还是行为变更 |
| 外部依赖版本不兼容 | 1 | ⚠️ pom 配置问题，可修复 |
| **合计** | **19** | — |

> 原始运行结果：`Tests run: 4824, Failures: 8, Errors: 11, Skipped: 0`。
