# Variant Shredding
UT: `GenericVariantTest`

统一以 JSON `{"a": 1, "b": "hello"}` 为例。

---

## Variant 由两部分组成

```java
GenericVariant(byte[] value, byte[] metadata, int pos)
```


| 部分       | 内容                                                |
| ---------- | --------------------------------------------------- |
| `value`    | Variant 二进制值（对象里存的是 key id，不是字符串） |
| `metadata` | 字段名字典（id → 字段名）                          |
| `pos`      | value 数组中当前 Variant 的起始位置，顶层为`0`      |

例：`{"a": 1, "b": "hello"}` 的 metadata 会记录 `0->"a"`, `1->"b"`。

---

## metadata

`[1, 2, 0, 1, 2, 97, 98]`


| 字节 | 值   | 含义                                                         |
| ---- | ---- | ------------------------------------------------------------ |
| 0    | `1`  | header：`version=1`（低 4 位），`offsetSize=1`（`(1-1)<<6`） |
| 1    | `2`  | `numKeys=2`，有两个字段名                                    |
| 2    | `0`  | id 0 的字符串起始偏移                                        |
| 3    | `1`  | id 1 的字符串起始偏移                                        |
| 4    | `2`  | 字符串结束偏移（总长度）                                     |
| 5    | `97` | ASCII`'a'`                                                   |
| 6    | `98` | ASCII`'b'`                                                   |

所以 `id 0 → "a"`，`id 1 → "b"`。

### metadata 的构建

`GenericVariantBuilder.result()` 负责把字段名字典序列化成 metadata 字节数组。

1. **value 和 metadata 是分开的两段字节**

   解析 JSON 时：
   - `value` 字节边解析边写入 `writeBuffer`
   - `metadata` 字典遇到新字段名就调用 `addKey()` 分配 id
   - 最后调用 `result()` 时，才根据 `dictionaryKeys` 生成 metadata 字节数组

   所以顺序是：**先写 value，后生成 metadata**。

2. **id 按字段首次出现顺序分配**

   `addKey()` 逻辑：id 由字段名首次出现的顺序决定。

3. **value 里的 id 列表和 offset 列表按 key 排序，data 保持写入顺序**

   `finishWritingObject()` 会按字段名排序的是 **id 列表和 offset 列表**，而字段的实际 **data 保持最初写入的顺序**。

   为什么这样设计？因为移动 data 需要拷贝字节，成本高；而 id 和 offset 只是几个整数，排序开销小。

   对 `{"b": 1, "a": 2}`：

   - 解析顺序：先遇到 `"b"`，后遇到 `"a"`
   - data 写入顺序：`"b"=1` 先写，`"a"=2` 后写
   - 但 id 列表和 offset 列表按 key 排序：先 `"a"`，后 `"b"`

   value 实际布局（假设 `objectHeader(false, 1, 1)`，即 header 占 1B，size 占 1B，id/offset 各 1B）：

   value 中实际存储：
   <table>
     <tr>
       <th colspan="1">header</th>
       <th colspan="1">size</th>
       <th colspan="2">字典顺序</th>
       <th colspan="3">offset 按字典排序</th>
       <th colspan="2">data 写入顺序</th>
     </tr>
     <tr>
       <td>1B</td>
       <td>1B</td>
       <td>1B</td>
       <td>1B</td>
       <td>1B</td>
       <td>1B</td>
       <td>1B</td>
       <td>2B</td>
       <td>2B</td>
     </tr>
     <tr>
       <td>0x02<br/>OBJECT</td>
       <td>2<br/>num</td>
       <td>id=1<br/>"a"</td>
       <td>id=0<br/>"b"</td>
       <td>off=2<br/>"a"</td>
       <td>off=0<br/>"b"</td>
       <td>off=4<br/>end</td>
       <td>"b"=1<br/>数据</td>
       <td>"a"=2<br/>数据</td>
     </tr>
   </table>

   - `header = 0x02`：`objectHeader(false, 1, 1)`，OBJECT 类型，size/id/offset 各 1 字节
   - `size = 2`：字段数量为 2
   - 每个 id 占 1B，每个 offset 占 1B
   - data 中 `"b"=1` 和 `"a"=2` 各用 INT1 编码：`[12, value]` 占 2B

#### 例子：`{"b": 1, "a": 2}`

**第一步：收集字段名，分配 id**

按出现顺序：先 `"b"`，后 `"a"`。

```text
dictionaryKeys = ["b", "a"]
id: "b" → 0, "a" → 1
```

**第二步：写入 value**

value 里的 **data 按 JSON 中出现顺序写入**：先写 `"b"=1`，后写 `"a"=2`。  
但 **id 列表和 offset 列表按 key 排序**：先 `"a"`，后 `"b"`。

```text
value 中 data 顺序: ["b"=1, "a"=2]
value 中 id/offset 列表顺序: ["a"(id=1), "b"(id=0)]
```

对应的 value 二进制：

```text
[header:1B][size:1B=2][id:1B=1][id:1B=0][off:1B=2][off:1B=0][off:1B=4]["b"=1 数据:2B]["a"=2 数据:2B]
```

总长度 = 1 + 1 + 2 + 3 + 4 = 11 字节。

**第三步：生成 metadata**

metadata 按 `dictionaryKeys = ["b", "a"]` 的顺序序列化。

- 决定 `offsetSize`

  ```java
  long maxSize = Math.max(dictionaryStringSize, numKeys);  // max(2, 2) = 2
  int offsetSize = getIntegerSize((int) maxSize);          // 2 可用 1 字节表示，所以 = 1
  ```

- metadata 结构

  ```text
  [header: 1B][dictSize: offsetSize][offsets: (numKeys+1)*offsetSize][string data]
  ```

- 填充字节

  | 步骤     | 计算                                                         | 结果   |
  | -------- | ------------------------------------------------------------ | ------ |
  | header   | `offsetSize-1 (2 bits) / 保留 (2 bits) / version (4 bits)`   | `0x01` |
  | dictSize | `numKeys = 2`                                                | `0x02` |
  | off0     | `"b"` 起始偏移                                               | `0x00` |
  | off1     | `"a"` 起始偏移                                               | `0x01` |
  | off2     | 字符串结束偏移                                               | `0x02` |
  | string   | `'b'`                                                        | `0x62` |
  | string   | `'a'`                                                        | `0x61` |

  - `version` 固定为 1，占低 4 位
  - `offsetSize` 存 `offsetSize - 1`，占高 2 位
  - 读取时：`offsetSize = ((metadata[0] >> 6) & 0x3) + 1`

- 最终 metadata

  ```text
  索引:    0     1     2     3     4     5     6
         0x01  0x02  0x00  0x01  0x02  0x62  0x61
           │     │     │     │     │     │     │
          ver   size  off0  off1  off2   b     a
  ```

**第四步：读取时如何对应**

value 里 `"a"` 存的是 id=1， `"b"` 存的是 id=0。用 id 查 metadata：

```text
id=1 → metadata[1] → "a"
id=0 → metadata[0] → "b"
```

所以即使 JSON 里字段顺序是 `{"b", "a"}`，通过排序后的 id/offset 列表也能正确还原为 `{"a": 2, "b": 1}`。

### metadata 解析

`GenericVariantUtil.getMetadataKey(metadata, id)` 根据 id 从 metadata 中反查字段名字符串。

metadata 还是 `[0x01, 0x02, 0x00, 0x01, 0x02, 0x62, 0x61]`：

| 步骤 | 计算 | 结果 |
|------|------|------|
| offsetSize | `(0x01 >> 6 & 0x3) + 1` | `1` |
| dictSize | `readUnsigned(metadata, 1, 1)` | `2` |
| stringStart | `1 + (2 + 2) * 1` | `5` |
| offset | `readUnsigned(metadata, 1 + 1*1, 1) = metadata[2]` | `0` |
| nextOffset | `readUnsigned(metadata, 1 + 2*1, 1) = metadata[3]` | `1` |
| 字符串 | `new String(metadata, 5 + 0, 1)` | `"b"` |

#### 例子：查 id=1（对应 `"a"`）

| 步骤 | 计算 | 结果 |
|------|------|------|
| offset | `readUnsigned(metadata, 1 + 2*1, 1) = metadata[3]` | `1` |
| nextOffset | `readUnsigned(metadata, 1 + 3*1, 1) = metadata[4]` | `2` |
| 字符串 | `new String(metadata, 5 + 1, 1)` | `"a"` |

所以 value 里存 id=0 → 查 metadata 得 `"b"`，id=1 → 查 metadata 得 `"a"`。

## Value 二进制解析

### Header 编码规则

每个 value 以 1 字节 header 开头，结构为：

```text
| type_info (6 bits) | basic_type (2 bits) |
```

`basic_type` 决定大类：


| basic_type      | 含义                                                             |
| --------------- | ---------------------------------------------------------------- |
| `0` (PRIMITIVE) | 标量：null/bool/int/double/string/date/timestamp/decimal/uuid 等 |
| `1` (SHORT_STR) | 短字符串，`type_info` 直接是长度 `[0, 63]`                       |
| `2` (OBJECT)    | 对象/struct                                                      |
| `3` (ARRAY)     | 数组                                                             |

对应代码：

```
// PRIMITIVE = 0, SHORT_STR = 1, OBJECT = 2, ARRAY = 3
primitiveHeader(type) = type << 2 | PRIMITIVE   // 低 2 位 = 00
shortStrHeader(size)  = size  << 2 | SHORT_STR   // 低 2 位 = 01
objectHeader(...)                                // 低 2 位 = 10
arrayHeader(...)                                 // 低 2 位 = 11
```

当 `basic_type = 0`（PRIMITIVE）时，`type_info` 表示具体标量类型：


| type_info             | 类型                            | 内容大小                             |
| --------------------- | ------------------------------- | ------------------------------------ |
| `0`                   | null                            | 仅 header                            |
| `1` / `2`             | true / false                    | 仅 header                            |
| `3` / `4` / `5` / `6` | int8 / int16 / int32 / int64    | header + 1/2/4/8 bytes               |
| `7`                   | double                          | header + 8 bytes                     |
| `8` / `9` / `10`      | decimal4 / decimal8 / decimal16 | header + 1 byte scale + 4/8/16 bytes |
| `11`                  | date                            | header + 4 bytes                     |
| `12` / `13`           | timestamp / timestamp_ntz       | header + 8 bytes                     |
| `14`                  | float                           | header + 4 bytes                     |
| `15`                  | binary                          | header + 4 bytes长度 + 内容          |
| `16`                  | long string                     | header + 4 bytes长度 + 内容          |
| `20`                  | UUID                            | header + 16 bytes                    |

例如 `primitiveHeader(INT1)`：`INT1 = 3`，`3 << 2 | 0 = 0b00001100 = 12`。

### OBJECT / ARRAY 的 header 编码

OBJECT 和 ARRAY 的 header 除了标明自己是对象/数组外，还把**字段数占几个字节**、**id 占几个字节**、**偏移占几个字节**这些信息也编码进去。

### `largeSize`、`idSize`、`offsetSize` 是什么意思


| 参数         | 含义                            | 取值                              |
| ------------ | ------------------------------- | --------------------------------- |
| `largeSize`  | 字段/元素数量用几个字节存       | `false` = 1 字节，`true` = 4 字节 |
| `idSize`     | OBJECT 里每个字段 id 占几个字节 | 1 / 2 / 3 / 4                     |
| `offsetSize` | 每个偏移占几个字节              | 1 / 2 / 3 / 4                     |

目的：**变长编码，小对象省空间，大对象能表示更大的值**。

#### 例子：同样存 `{"a": 1, "b": 2}`

**小对象**（2 个字段，数据很短）：

```text
largeSize=false  → size 占 1 字节
idSize=1         → 每个 id 占 1 字节
offsetSize=1     → 每个 offset 占 1 字节
```

二进制布局（`a=1` 和 `b=2` 都用 INT1 编码：1 字节 header + 1 字节值）：

```text
| header | size |   id list     |        offset list       |      data         |
|   1B   |  1B  | id0=0 | id1=1 | off0=0 | off1=2 | off2=4 | [12, 1] | [12, 2] |
|        |      |  1B   |  1B   |   1B   |   1B   |   1B   |   2B    |   2B    |
```

- `id list`：字段 id 列表，`id0=0` 对应 `"a"`，`id1=1` 对应 `"b"`
- `offset list`：每个字段在 data 中的起始偏移，最后一个偏移 `off2=4` 表示 data 总长度
- `data`：字段的实际值。`[12, 1]` 是 `"a"` 的值，`12 = primitiveHeader(INT1)` 是 header，`1` 是具体值

所以 `"a"` 占 2 字节，`"b"` 占 2 字节，偏移分别是 `0`、`2`、`4`。

**大对象**（假设有 300 个字段，数据总长超过 255 字节）：

```text
largeSize=true   → size 占 4 字节（300 > 255，1 字节存不下）
idSize=2         → 每个 id 占 2 字节
offsetSize=4     → 每个 offset 占 4 字节
```

二进制布局：

```text
[header][size=4B][id*2B * 300][offset*4B * 301][数据...]
```

#### OBJECT header 例子

`objectHeader(largeSize, idSize, offsetSize)`：
以 `objectHeader(false, 2, 1) = 18 = 0b00010010` 为例拆解：

```text
0b00010010
   │││││└┴─ OBJECT = 2 (低 2 位固定为 10)
   │││└┴─── offsetSize-1 = 0 → offsetSize = 1
   │└┴───── idSize-1 = 1 → idSize = 2
   └─────── largeSize = 0 → 字段数占 1 字节
```

#### ARRAY 没有 `idSize`

数组元素按顺序访问，不需要 id，所以 `arrayHeader` 只有 `largeSize` 和 `offsetSize`。

#### ARRAY header 例子

`arrayHeader(largeSize, offsetSize)`：


| 调用                    | 计算    | header 值 | 二进制       | 含义               |
| ----------------------- |---------|-----------| ------------ | ------------------ |
| `arrayHeader(false, 1)` | `0\<\<2 | 0\<\<2    | 3` | `3`       | `0b00000011` | size=1B, offset=1B |
| `arrayHeader(true, 4)`  | `1\<\<2 | 3\<\<2    | 3` | `31`      | `0b00011111` | size=4B, offset=4B |

以 `arrayHeader(true, 4) = 31 = 0b00011111` 为例拆解：

```text
0b00011111
    ││││└┴─ ARRAY = 3 (低 2 位固定为 11)
    ││└┴─── offsetSize-1 = 3 → offsetSize = 4
    └┴───── largeSize = 1 → 元素数占 4 字节
```

### 例子: value

`[2, 2, 0, 1, 0, 2, 8, 12, 1, 21, 104, 101, 108, 108, 111]`

| 字节  | 值                    | 含义                                                                                 |
| ----- | --------------------- | ------------------------------------------------------------------------------------ |
| 0     | `2`                   | object header。低 2 位`10` = OBJECT；类型信息 `2>>2=0` 表示 size/id/offset 各 1 字节 |
| 1     | `2`                   | 字段数量 = 2                                                                         |
| 2     | `0`                   | 字段 0 的 id（对应 metadata 里的`"a"`）                                              |
| 3     | `1`                   | 字段 1 的 id（对应 metadata 里的`"b"`）                                              |
| 4     | `0`                   | 字段 0 的数据偏移                                                                    |
| 5     | `2`                   | 字段 1 的数据偏移                                                                    |
| 6     | `8`                   | 数据总长度                                                                           |
| 7     | `12`                  | `"a"` 的 `primitiveHeader(INT1)`。`INT1=3`，`3<<2`                                    |
| 8     | `1`                   | `"a"` 的值 = 1                                                                       |
| 9     | `21`                  | `"b"` 的 `shortStrHeader(5)`。字符串长度 5，`5<<2`                                    |
| 10-14 | `104,101,108,108,111` | `"b"` 的字符串内容 `"hello"`                                                         |

#### 解码步骤（以 `value[0] = 2` 为例）

拿到一个 header 字节，这样解：

1. **最低 2 位**决定类型：

   ```
   2 & 0b00000011 = 2 → OBJECT
   ```
2. **右移 2 位**得到 `type_info`：

   ```
   2 >> 2 = 0 = 0b000000
   ```
3. 对 OBJECT，再按位拆分 `type_info`：

   ```
   offsetSize = (type_info & 0b000011) + 1 = 0 + 1 = 1
   idSize     = (type_info >> 2 & 0b000011) + 1 = 0 + 1 = 1
   largeSize  = (type_info >> 4 & 0b000001) = 0
   ```

所以 `value[0] = 2` 表示：这是一个对象，字段数占 1 字节，id 占 1 字节，offset 占 1 字节。

---

## 为什么要 Shredding

Variant 是半结构化二进制，查询时每次都要解析。
Shredding 把常用字段拆成**强类型列**存储，查询时直接读列，更快。
同时保留一份原始 Variant `value`，保证能无损还原。

---

## Schema
### shreddedType：用户想要的结构

`ROW<`a` INT, `b` STRING>`

含义：把 `a` 当 `INT`，`b` 当 `STRING` 拆出来。

### shreddingSchema：实际落盘的物理结构

```sql
ROW<
  `metadata` BYTES NOT NULL,
  `value` BYTES,
  `typed_value` ROW<
    `a` ROW<`value` BYTES, `typed_value` INT> NOT NULL,
    `b` ROW<`value` BYTES, `typed_value` STRING> NOT NULL
  >
>
```

每一层都带 `value` + `typed_value`：

- `typed_value`：拆出来的强类型值
- `value`：没拆成功时放原始 Variant

---

### VariantSchema：运行时表示

`buildVariantSchema(shreddingSchema)` 把物理 schema 转成运行时可操作的 `VariantSchema` 对象。

以 `{"a": 1, "b": "hello"}` + `struct<a:int, b:string>` 为例，生成的 `VariantSchema` 大致长这样：

```
VariantSchema{
   typedIdx=2, variantIdx=1, topLevelMetadataIdx=0, numFields=3, scalarSchema=null,
   objectSchema=[
      ObjectField{
         fieldName=a,
         schema=VariantSchema{
            typedIdx=1, variantIdx=0, topLevelMetadataIdx=-1, numFields=2,
            scalarSchema=IntegralType,
            objectSchema=null, arraySchema=null
         }
      },
      ObjectField{
         fieldName=b,
         schema=VariantSchema{
            typedIdx=1, variantIdx=0, topLevelMetadataIdx=-1, numFields=2,
            scalarSchema=StringType,
            objectSchema=null, arraySchema=null
         }
      }
   ],
   objectSchemaMap { a -> 0, b -> 1 }
}
```

#### 顶层 schema 解释

顶层对应 shredding schema：

```sql
ROW<metadata BYTES, value BYTES, typed_value ROW<...>>
```

| 字段                           | 值                      | 含义 |
|--------------------------------|-------------------------|------|
| `topLevelMetadataIdx=0`        | `metadata` 在第 0 列    | 顶层才有，子层为 `-1` |
| `variantIdx=1`                 | `value` 在第 1 列       | 存未拆的原始 Variant |
| `typedIdx=2`                   | `typed_value` 在第 2 列 | 存拆出来的结构化数据 |
| `numFields=3`                  | 这一层有 3 列           | `metadata` + `value` + `typed_value` |
| `scalarSchema=null`            | 顶层不是标量            | 因为顶层是对象 |
| `objectSchema=[...]`           | 顶层是对象，有两个字段  | `"a"` 和 `"b"` |
| `objectSchemaMap={a->0, b->1}` | 字段名到数组下标的映射  | 快速查找用 |

#### 字段 `"a"` 的 schema 解释

`"a"` 对应内层 schema：

```sql
ROW<value BYTES, typed_value INT>
```

```
VariantSchema{
   typedIdx=1, variantIdx=0, topLevelMetadataIdx=-1, numFields=2,
   scalarSchema=IntegralType,
   objectSchema=null, arraySchema=null
}
```

| 字段 | 值 | 含义 |
|------|-----|------|
| `topLevelMetadataIdx=-1` | 子层没有 `metadata` | 只有顶层有 |
| `variantIdx=0` | `value` 在第 0 列 | 存 `"a"` 未拆的原始 Variant |
| `typedIdx=1` | `typed_value` 在第 1 列 | 存 `"a"` 拆出来的 int |
| `numFields=2` | 这一层有 2 列 | `value` + `typed_value` |
| `scalarSchema=IntegralType` | `"a"` 是整型 | 拆的时候按 int 处理 |

#### 字段 `"b"` 的 schema 解释

和 `"a"` 几乎一样，只是 `scalarSchema=StringType`，表示 `"b"` 按字符串处理。

### 嵌套结构怎么办？

`shreddedType` 是分层定义的，每一层用 `ROW` 或 `ARRAY` 描述。不同层次的同名字段是完全独立的。

例如 JSON：

```json
{
  "a": 1,
  "b": {
    "a": "hello"
  }
}
```

想同时拆外层 `"a"` 和内层 `"b.a"`，`shreddedType` 要这样写：

```
ROW<`a` INT, `b` ROW<`a` STRING>>
```

展开后：

- 顶层 `"a"` 是 `INT`
- 顶层 `"b"` 是 `ROW`，里面的 `"a"` 是 `STRING`

这两个 `"a"` 处于不同层级， shredding schema 会分别处理，不会冲突。

对应的 `shreddingSchema` 大概长这样：

```
ROW<
  `metadata` BYTES NOT NULL,
  `value` BYTES,
  `typed_value` ROW<
    `a` ROW<`value` BYTES, `typed_value` INT> NOT NULL,
    `b` ROW<
      `value` BYTES,
      `typed_value` ROW<
        `a` ROW<`value` BYTES, `typed_value` STRING> NOT NULL
      >
    > NOT NULL
  >
>
```

---

## Variant → 拆好的 Row
递归遍历 Variant：
- 字段在 schema 里 → 拆到 `typed_value`
- 字段不在 schema 里 → 塞进 `value`
- 类型不匹配 → 塞进 `value`

例如：
```java
InternalRow shredded = castShredded(variant, variantSchema);
```
`castShredded` 拿到 `variantSchema` 后：

1. 看顶层 `topLevelMetadataIdx=0`，把 `variant.metadata()` 写到 row 第 0 列
2. 看顶层 `objectSchema`，遍历 Variant 对象的字段
3. 对于 `"a"`：
   - 用 `objectSchemaMap.get("a")` 快速拿到数组下标 0
   - 递归用 `"a"` 的 schema 去拆
   - 把类型不匹配的 `value` 写到子 row 第 0 列，没有问题的`typed_value` 写到第 1 列
4. 最后把 `"a"`、`"b"` 的子 row 组合成顶层 `typed_value`，写到顶层 row 第 2 列


`{"a": 1, "b": "hello"}` + `struct<a:int, b:string>`：

```text
+I([1,2,0,1,2,97,98], null, +I(+I(null,1), +I(null,hello)))
```

| 字段                        | 值                  | 含义                       |
| --------------------------- | ------------------- | -------------------------- |
| `metadata`                  | `[1,2,0,1,2,97,98]` | 字段名字典                 |
| `value`                     | `null`              | 没有未拆数据               |
| `typed_value.a.value`       | `null`              | `a` 完美匹配，不需要原始值 |
| `typed_value.a.typed_value` | `1`                 | 拆出来的 int               |
| `typed_value.b.value`       | `null`              | `b` 完美匹配               |
| `typed_value.b.typed_value` | `hello`             | 拆出来的 string            |

---

## 拆好的 Row → Variant TODO

```java
Variant rebuild = assembleVariant(shredded, variantSchema);
```

把 `typed_value` 和 `value` 重新拼回完整的 Variant，再 `toJson()` 可还原原始 JSON。

---


## null vs missing vs typed

`typed_value` 里的对象字段 row 都是 `NOT NULL`，靠内部两个字段区分状态：


| 状态             | `value`                                    | `typed_value` | 对应 JSON         | 说明                                            |
|------------------|--------------------------------------------|---------------|-------------------|-------------------------------------------------|
| 有值且匹配       | `null`                                     | `1`           | `{"a": 1}`        | schema 是 INT，值也是 int，完美匹配             |
| 有值但类型不匹配 | 原始 Variant 字节：`[104,101,108,108,111]` | `null`        | `{"a": "hello"}`  | schema 是 INT，但值是 string，存原始值 fallback |
| 有值但为 null    | `[0]`                                      | `null`        | `{"a": null}`     | 字段存在，值是 JSON null                        |
| 字段缺失         | `null`                                     | `null`        | `{}`              | JSON 里没有这个字段                             |


这样虽然没拆成 INT，但后续 `assembleVariant` 还能从 `value` 里把原始 JSON 还原出来，不会丢数据。

