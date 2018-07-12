
### identity

```
scala> val a: Array[Int] = Array(1, 12, 3, 4, 1)
a: Array[Int] = Array(1, 12, 3, 4, 1)

scala> a.groupBy(i=>i)
res0: scala.collection.immutable.Map[Int,Array[Int]] = Map(4 -> Array(4), 1 -> Array(1, 1), 3 -> Array(3), 12 -> Array(12))

scala> a.groupBy(identity)
res1: scala.collection.immutable.Map[Int,Array[Int]] = Map(4 -> Array(4), 1 -> Array(1, 1), 3 -> Array(3), 12 -> Array(12))
```