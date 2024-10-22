package cn.juntaozhang.codility2

import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  *
  */
class DP extends FunSuite with BeforeAndAfterAll {

  test("number solitaire") {
    def solution(A: Array[Int]): Int = {
      // dp[i] = max(dp[i-1],dp[i-2],dp[i-3],dp[i-4],dp[i-5],dp[i-6]) + A[i]
      if (A.length == 0) return 0
      val dp = new Array[Int](A.length)
      dp(0) = A(0)
      for (i <- 1 until A.length) {
        dp(i) = dp(i - 1)
        for (j <- i - 6 until i - 1 if j >= 0) {
          dp(i) = dp(i).max(dp(j))
        }
        dp(i) += A(i)
      }
      println(A.mkString(","))
      println(dp.mkString(","))
      dp(A.length - 1)
    }

    println(solution(Array[Int](1, -2, 0, 9, -1, -2, -1, -1, -1, 0, 0, 1)))
  }


}
