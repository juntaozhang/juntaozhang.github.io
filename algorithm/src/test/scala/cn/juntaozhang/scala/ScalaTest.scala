package cn.juntaozhang.scala

import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  *
  */
class ScalaTest extends FunSuite with BeforeAndAfterAll {

  case class Name(val f: String, val l: String)

  test("unapply") {
    object Name {
      def unapply(input: String): Option[Seq[String]] = if (input.trim == "") None else Some(input.trim.split("\\s+"))
    }
  }
  test("test control abstraction") {
    def until(c: => Boolean)(f: => Unit) {
      if (c) {
        f
        until(c)(f)
      }
    }

    var x = 10
    until(x != 0) {
      x -= 1
      println(x)
    }

    //    x = 10
    //    while (x != 0) {
    //      x -= 1
    //      println(x)
    //    }
    println("success")
  }

}
