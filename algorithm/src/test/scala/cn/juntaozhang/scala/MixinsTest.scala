package cn.juntaozhang.scala

import org.junit.Test
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  *
  */
abstract class A {
  val message: String
}

class B extends A {
  val message = "I'm an instance of class B"
}

trait C extends A {

  def loudMessage = message.toUpperCase()
}

class D extends B with C

object MixinsTest extends App {
  //  val d = new D
  //  d.message // I'm an instance of class B
  //  d.loudMessage

  for (i <- (0 until(10, 3)).reverse) {
    println(i)
  }


  def mulBy(f: Double)(x: Double): Double = {
    val a = (m: Double) => m * f

    a(x)
  }

  //return a function
  def mulBy2(f: Double) = (m: Double) => m * f

  def mulBy3(f: Double): (Double) => Double = (m: Double) => m * f


}
