package com.stackmob.customcode.dev
package test
package server
package sdk
package simulator

import org.specs2.{ScalaCheck, Specification}
import com.stackmob.customcode.dev.server.sdk.simulator.CallLimitation
import scala.util.Try
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen

class CallLimitationSpecs extends Specification with CustomMatchers with ScalaCheck { def is =
  "CallLimitationSpecs".title                                                                                           ^ end ^
  "CallLimitation tracks the number of calls to a method or some methods, and then throws if over a high watermark"     ^ end ^
  "CallLimitation always throws with a 0 limit"                                                                         ! alwaysThrows ^
  "CallLimitation never throws when under the limit"                                                                    ! worksUnderLimit ^
                                                                                                                        end
  private val ex = new Exception("hello world")
  private def alwaysThrows = {
    val limiter = CallLimitation(0) { lst =>
      ex
    }
    Try(limiter("should throw") { "hello world" }).toEither must beThrowable(ex)
  }

  private lazy val genLimit = Gen.choose(1, 100)
  private def worksUnderLimit = forAll(genLimit) { limit =>
    val limiter = CallLimitation(limit) { lst =>
      ex
    }
    val res = "abc"
    val results = (0 until (limit -1)).map { num =>
      limiter("normal")(res)
    }
    results.size must beEqualTo(limit-1)
  }
}
