package cn.juntaozhang.example.spark

import org.scalatest.funsuite.AnyFunSuite

class ScalaGrammarSuite extends AnyFunSuite {
  test("PartialFunction") {
    val fn: PartialFunction[String, Int] = {
      case s if s.forall(_.isDigit) => s.toInt
    }

    println(fn.isDefinedAt("123")) // 输出: true
    println(fn("123")) // 输出: 123

    println(fn.isDefinedAt("abc")) // 输出: false
    // println(fn("abc")) // 这会抛出 MatchError，因为 "abc" 不在偏函数的定义域内
  }

  test("Function.unlift") {
    val tryParseInt: String => Option[Int] = s => {
      try {
        Some(s.toInt)
      } catch {
        case _: NumberFormatException => None
      }
    }

    def tryParseInt2(s: String): Option[Int] = {
      try {
        Some(s.toInt)
      } catch {
        case _: NumberFormatException => None
      }
    }


    /**
     * Lift：将一个值从一个较低的类型层次结构提升到一个较高的类型层次结构。
     * Unlift：将一个值从一个较高的类型层次结构降低到一个较低的类型层次结构。
     */
    // 使用 Function.unlift 将 tryParseInt 转换为一个偏函数
    val parseIntPF: PartialFunction[String, Int] = Function.unlift(tryParseInt2)

    // 使用偏函数
    println(parseIntPF.isDefinedAt("123")) // 输出: true
    println(parseIntPF("123")) // 输出: 123

    println(parseIntPF.isDefinedAt("abc")) // 输出: false
    // println(parseIntPF("abc"))             // 这会抛出 MatchError，因为 "abc" 不在偏函数的定义域内

  }

  // 使用lift将偏函数转换为普通函数
  test("lift") {
    // 定义一个偏函数，只对整数2和3定义
    val pf: PartialFunction[Int, String] = {
      case 2 => "two"
      case 3 => "three"
    }
    val liftedPf = pf.lift

    // 对于定义域内的值，返回Some(result)
    println(liftedPf(2)) // 输出: Some(two)
    println(liftedPf(3)) // 输出: Some(three)

    // 对于定义域外的值，返回None
    println(liftedPf(1)) // 输出: None
  }


  test("identity") {
    val number: Int = 5
    // identity 是一个预定义函数，返回值永远是参数本身
    val result: Int = identity(number)

    println(result) // 输出: 5
  }

  test("identity case2") {
    sealed trait SimpleTree
    case class Leaf(value: Int) extends SimpleTree
    case class Branch(left: SimpleTree, right: SimpleTree) extends SimpleTree

    def transformTree(tree: SimpleTree, transform: PartialFunction[SimpleTree, SimpleTree]): SimpleTree = tree match {
      case leaf: Leaf => transform.applyOrElse(leaf, identity[SimpleTree])
      case Branch(left, right) =>
        val newLeft = transformTree(left, transform)
        val newRight = transformTree(right, transform)
        Branch(newLeft, newRight)
    }

    val tree = Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))
    val doubleEvenValues: PartialFunction[SimpleTree, SimpleTree] = {
      case Leaf(value) if value % 2 == 0 => Leaf(value * 2)
    }

    val transformedTree = transformTree(tree, doubleEvenValues)
    println(tree)
    println(transformedTree)
  }

  test("lambda") {
    def fun0(): Long = {
      1L
    }

    def myprint(f: () => Long): Unit = {
      println(f())
    }

    val fun1: () => Long = fun0

    println(fun0) // here fun0 will run and return 1L, equals to `println(fun0())`

    myprint(fun0) // here fun0 is fun, will pass the function to myprint
    myprint(() => fun0) // lambda function
    myprint(fun0 _)
  }
}
