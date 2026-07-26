# geohash
二维坐标 → 一维数值

## 经纬度转换
- 编码：区间二分→ 二进制编码→ 位交叉→ Base32 转换
- 解码：Base32 转二进制→拆分经纬度二进制→区间还原→计算中心点
  - 经度区间还原（二进制110100）：最终得到经度范围[112.5, 118.125]
  - 纬度区间还原（二进制101110）：最终得到纬度范围[39.375, 42.1875]
  - 经度区间还原（二进制110100）：最终得到经度范围[112.5, 118.125]

### Base32
是32进制编码，每个字符表示5位二进制数
[0-9] +[A-Z] 去掉 A、O、I、L

![范围检索思路](https://pic3.zhimg.com/v2-a2367bce85a3042881be976781a9e612_1440w.jpg)
## 相邻块
- 先解码 Geohash 得到其对应的经纬度范围，再计算相邻区块的经纬度范围
    - 8位Geohash的经度步长≈0.0012km，纬度步长≈0.0006km
    - Geohash 的相邻块包括 8 个方向（上、下、左、右、左上、左下、右上、右下）

## zorder
与 geohash 原理相同，只是不是解决经纬度检索问题，而是解决多维检索。\
即：**多维空间中相邻的点，转成一维编码后，大概率也相邻。**

应用：是否能够筛选出来更少的待读取的文件？

![是否能够筛选出来更少的待读取的文件](https://izualzhy.cn/assets/images/LinerOrderVSZOrder.png)

- https://izualzhy.cn/lakehouse-zorder

## Reference
- geohash 工具：https://geohash.softeng.co/wtw3sxqh 
- GeoHash 技术原理及应用实战
  - https://www.bilibili.com/video/BV1Zg4y1179b
  - [对应知乎文章](https://zhuanlan.zhihu.com/p/645078866)
