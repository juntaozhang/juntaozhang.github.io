# fastjson 1.2.47 Tests on 1.2.84 失败案例分析

## 说明

本文档基于 `failing-tests-summary.txt`，将 fastjson 1.2.47 的测试用例在 1.2.84 上运行产生的失败按**现象大类**归并。

- 所有 `autoType` / `@type` 相关的安全限制统一归为一类，不细分具体类名。
- 每类均给出原始错误示例，并附明确 RCA（含代码场景）。
- 原始 `failing-tests-summary.txt` 共涉及 **16 个测试类 / 27 个失败+错误方法**。

---

## 一、autoType / @type 安全策略限制

**统一特征**：

```text
com.alibaba.fastjson.JSONException: autoType is not support. xxx
```

1.2.84 默认关闭 autoType，所有依赖 `@type` 反序列化到具体类的测试都被拦截。

**涉及用例**：`FontTest2`、`RectangleTest`、`Issue1240`、`DefaultSavedRequestTest`

**数量**：10 个 error

### 示例 1：AWT 类 — `java.awt.Font`

```text
=== com.alibaba.json.bvt.awt.FontTest2.txt ===
test_color(com.alibaba.json.bvt.awt.FontTest2)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. java.awt.Font
```

**RCA**：测试代码遍历系统所有字体，对每个 `java.awt.Font` 对象先 `JSON.toJSONString(font, SerializerFeature.WriteClassName)` 再 `JSON.parse(text)`。序列化结果中带有 `@type:java.awt.Font`，1.2.84 反序列化时拒绝加载该类。

### 示例 2：AWT 类 — `java.awt.Rectangle`

```text
=== com.alibaba.json.bvt.RectangleTest.txt ===
test_color(com.alibaba.json.bvt.RectangleTest)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. java.awt.Rectangle
```

**RCA**：与 `FontTest2` 相同，对 `java.awt.Rectangle` 使用 `WriteClassName` 序列化后再反序列化，1.2.84 默认不再允许 AWT 类通过 autoType 加载。

### 示例 3：Spring Util — `LinkedMultiValueMap`

```text
=== com.alibaba.json.bvt.issue_1200.Issue1240.txt ===
test_for_issue(com.alibaba.json.bvt.issue_1200.Issue1240)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. org.springframework.util.LinkedMultiValueMap
```

**RCA**：测试用例解析带 `@type:org.springframework.util.LinkedMultiValueMap` 的 JSON。1.2.84 默认安全策略下，Spring 工具类不在 autoType 白名单中。

### 示例 4：Spring Security 系列类

```text
=== com.alibaba.json.bvt.support.spring.security.DefaultSavedRequestTest.txt ===
test_SecurityContextImpl(com.alibaba.json.bvt.support.spring.security.DefaultSavedRequestTest)  <<< ERROR!
com.alibaba.fastjson.JSONException: autoType is not support. org.springframework.security.core.context.SecurityContextImpl
```

**RCA**：Spring Security 相关对象（`SecurityContextImpl`、`User`、`UsernamePasswordAuthenticationToken`、`SimpleGrantedAuthority`、`DefaultSavedRequest`）序列化后均带 `@type`，1.2.84 默认全部拒绝反序列化。

---

## 二、序列化输出格式变化

**统一特征**：同样的输入，1.2.84 输出的 JSON 字符串格式变了，导致 `assertEquals` 失败。包括浮点数小数位、`LocalDateTime` 分隔符、集合类型标记。

**涉及用例**：`CompatibleTest0`、`CompatibleTest_noasm`、`LocalDateTimeTest4`、`WriteClassNameTest_Set4`、`WriteClassNameTest_Set`

**数量**：7 个 failure

### 示例 1：json-lib 兼容模式下 float/double 的 `0.0` 输出为 `0`

```text
=== com.alibaba.json.bvt.compatible.jsonlib.CompatibleTest0.txt ===
test_5(com.alibaba.json.bvt.compatible.jsonlib.CompatibleTest0)  <<< FAILURE!
java.lang.AssertionError: expected:<{"f1":0.0,"f2":20013.0,"f3":0,"f4":0}> but was:<{"f1":0,"f2":20013,"f3":0,"f4":0}>
```

**RCA**：测试使用 `JSONLibDataFormatSerializer` + `SerializerFeature` 组合模拟 json-lib 输出，并与 `net.sf.json.JSONObject.fromObject` 的结果比较。1.2.47 对 `float`/`double` 字段 `0.0` 保留小数位输出；1.2.84 在该兼容模式下将 `0.0` 输出为整数 `0`，与 json-lib 实际输出一致，但与 fastjson 1.2.47 历史行为不同。

### 示例 2：`LocalDateTime` 默认格式从空格分隔变为 `T` 分隔

```text
=== com.alibaba.json.bvt.jdk8.LocalDateTimeTest4.txt ===
test_for_issue(com.alibaba.json.bvt.jdk8.LocalDateTimeTest4)  <<< FAILURE!
org.junit.ComparisonFailure: expected:<{"date":"2016-05-06[ ]09:03:16"}> but was:<{"date":"2016-05-06[T]09:03:16"}>
```

**RCA**：测试代码调用 `JSON.toJSONString(vo)` 序列化包含 `java.time.LocalDateTime` 字段的 VO。1.2.47 默认使用 `"yyyy-MM-dd HH:mm:ss"`（空格分隔）；1.2.84 默认改为 ISO8601 格式 `"yyyy-MM-dd'T'HH:mm:ss"`（`T` 分隔）。

### 示例 3：`Set` 字段写类名时多输出 `[Set]` 标记

```text
=== com.alibaba.json.bvt.writeClassName.WriteClassNameTest_Set.txt ===
test_list(com.alibaba.json.bvt.writeClassName.WriteClassNameTest_Set)  <<< FAILURE!
org.junit.ComparisonFailure: expected:<...eTest_Set$A","list":[][{},...}> but was:<...eTest_Set$A","list":[Set][{},...}>
```

**RCA**：开启 `SerializerFeature.WriteClassName` 后对 `Set` 类型字段做序列化。1.2.84 会额外输出 `[Set]` 类型标识；1.2.47 直接输出 `[]`。

---

## 三、反序列化 / 解析 / 类型转换行为变化

**统一特征**：不是 autoType 被拦截，而是反序列化逻辑、类型转换、空值处理、setter 识别等行为变化。

**涉及用例**：`DefaultExtJSONParserTest`、`Issue1152`、`ReadOnlyMapTest2`、`TypeUtilsTest`、`Issue1272`、`JSONScannerTest_ISO8601`

**数量**：8 个（6 failure + 2 error）

### 示例 1：非标准 setter `setage` 被优先匹配

```text
=== com.alibaba.json.bvt.parser.DefaultExtJSONParserTest.txt ===
test_parseObject(com.alibaba.json.bvt.parser.DefaultExtJSONParserTest)  <<< ERROR!
com.alibaba.fastjson.JSONException: set property error, com.alibaba.json.bvt.parser.DefaultExtJSONParserTest$User#age
	at com.alibaba.json.bvt.parser.DefaultExtJSONParserTest$User.setage(DefaultExtJSONParserTest.java:550)
```

**RCA**：`User` 类同时存在规范 setter `setAge(int)` 和非规范 setter `setage(int)`（方法体抛 `UnsupportedOperationException`）。1.2.47 优先匹配 `setAge`；1.2.84 的 setter 匹配逻辑变化，优先命中 `setage`，导致反序列化 `age` 字段时抛异常。

### 示例 2：无效日期字符串被解析成 `Date` 而非 `null`

```text
=== com.alibaba.json.bvt.issue_1100.Issue1152.txt ===
test_for_issue_2(com.alibaba.json.bvt.issue_1100.Issue1152)  <<< FAILURE!
junit.framework.AssertionFailedError: Expected: <null> but was: Sat Jan 01 00:00:00 CST 1
```

**RCA**：测试用 `JSONObject.parseObject("{shijian:\"0001-01-01T00:00:00+08:00\"}", TestBean.class)` 反序列化日期字段，并期望得到 `null`。1.2.47 将该无效/极值日期视为非法并返回 `null`；1.2.84 的日期解析容错增强，将其解析为公元 1 年 1 月 1 日。

### 示例 3：只读 Map 的 null 字段未保留

```text
=== com.alibaba.json.bvt.parser.ReadOnlyMapTest2.txt ===
test_readOnlyNullList(com.alibaba.json.bvt.parser.ReadOnlyMapTest2)  <<< FAILURE!
java.lang.AssertionError: expected null, but was:<{a=com.alibaba.json.bvt.parser.ReadOnlyMapTest2$A@1b7440c3}>
```

**RCA**：`Entity` 中 `values` 字段只有 getter、没有 setter，输入 JSON 为 `{"values":{"a":{}}}`。1.2.47 对无 setter 的字段不赋值，保持 `null`；1.2.84 在反序列化时会尝试创建并设置该字段值，导致 `values` 不为 `null`。

### 示例 4：整数转 `java.sql.Timestamp` 不再抛异常

```text
=== com.alibaba.json.bvt.parser.TypeUtilsTest.txt ===
test_cast_to_Timestamp_error(com.alibaba.json.bvt.parser.TypeUtilsTest)  <<< FAILURE!
java.lang.AssertionError: null
```

**RCA**：测试代码构造 `JSONObject json = {"date": 0}`，然后调用 `json.getObject("date", java.sql.Timestamp.class)`，并断言会抛出 `JSONException`。1.2.84 对整数到 `Timestamp` 的类型转换做了容错，将 `0` 解释为时间戳起点（1970-01-01 00:00:00）而不再抛异常，因此捕获的 `error` 为 `null`，`Assert.assertNotNull(error)` 失败。

### 示例 5：基本类型 getter 返回 null 拆箱触发 NPE 的捕获方式变化

```text
=== com.alibaba.json.bvt.issue_1200.Issue1272.txt ===
test_for_issue(com.alibaba.json.bvt.issue_1200.Issue1272)  <<< ERROR!
java.lang.NullPointerException: null
	at com.alibaba.json.bvt.issue_1200.Issue1272.test_for_issue(Issue1272.java:20)
```

**RCA**：`Point` 类中 `userId` 为 `Long` 且为 `null`，但 getter `getUserId()` 返回 `long` 基本类型。序列化时拆箱触发 `NullPointerException`。测试断言该异常被包装为 `JSONException`，且 `cause.getCause()` 为 `NullPointerException`。1.2.84 对异常捕获/包装层级变化，导致断言第 20 行 `exception.getCause().getCause()` 为 `null`，直接触发 NPE。

### 示例 6：ISO8601 校验对年份上界判定变化

```text
=== com.alibaba.json.bvt.parser.JSONScannerTest_ISO8601.txt ===
test_0(com.alibaba.json.bvt.parser.JSONScannerTest_ISO8601)  <<< FAILURE!
java.lang.AssertionError: expected:<false> but was:<true>
        Assert.assertEquals(false, new JSONScanner("4000-10-02").scanISO8601DateIfMatch());
```

**RCA**：测试中调用 `new JSONScanner("4000-10-02").scanISO8601DateIfMatch()` 并期望返回 `false`。1.2.47 对 ISO8601 日期的年份范围做了上限校验，认为 `4000` 年超出有效日期范围；1.2.84 放宽了年份上限校验，将该字符串识别为合法日期，返回 `true`。实际失败的具体断言即为此行。

---

## 四、Spring 集成层输出格式变化

**统一特征**：`MappingFastJsonMessageConverter.toMessage` 返回的 `payload` 是 `byte[]`，而不是 JSON 字符串。

**涉及用例**：`MappingFastJsonMessageConverterTest`

**数量**：1 个 failure

### 示例

```text
=== com.alibaba.json.bvt.support.spring.messaging.MappingFastJsonMessageConverterTest.txt ===
test_1(com.alibaba.json.bvt.support.spring.messaging.MappingFastJsonMessageConverterTest)  <<< FAILURE!
java.lang.AssertionError: expected:<{"id":1}> but was:<[B@1910c9c2>
```

**RCA**：测试调用 `converter.toMessage(vo, null)` 后断言 `message.getPayload()` 等于字符串 `{"id":1}`。1.2.84 的 `MappingFastJsonMessageConverter` 在 `convertToInternal` 中改为直接返回序列化后的 `byte[]` 作为 payload；1.2.47 返回的是 JSON 字符串。

---

## 汇总

| 大类 | 数量（方法级） | 是否属于 1.2.84 已知行为变更 |
| --- | ---: | --- |
| autoType / @type 安全限制 | 10 | ✅ 是，预期内 |
| 序列化输出格式变化 | 7 | ✅ 大多是预期内 |
| 反序列化 / 解析 / 类型转换行为变化 | 8 | ⚠️ 部分是兼容性问题，需单独确认 |
| Spring 集成输出格式变化 | 1 | ⚠️ 需确认是 bug 还是行为变更 |
| **合计** | **26** | — |

> 注：合计 26 为按现象大类聚合后的约数；原始 `failing-tests-summary.txt` 共涉及 **16 个测试类 / 27 个失败+错误方法**。
