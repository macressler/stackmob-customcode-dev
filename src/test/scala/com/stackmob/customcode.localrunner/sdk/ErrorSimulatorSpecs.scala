package com.stackmob.customcode.localrunner.sdk

import org.specs2.Specification
import com.twitter.util.Duration
import org.specs2.mock.Mockito
import scala.util.Random

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 11:15 AM
 */
class ErrorSimulatorSpecs extends Specification with Mockito { def is =
  "ErrorSimulator".title                                                                                                ^
  "ErrorSimulator is a class to simulate N errors in a time window T"                                                   ^
  "if the error counter is at the maximum, run the operation normally"                                                  ! maxCounter ^ end ^
  "if the error counter is not at max, throw"                                                                           ! err ^ end ^
  "if the counter is not at max, execute normally"                                                                      ! normal ^ end ^
                                                                                                                        end
  private def freq(num: Int, every: Duration) = {
    new Frequency(num, every)
  }
  private val exception = {
    new Exception("test failure")
  }
  private val op = {
    "hello world"
  }
  private def rand(bool: Boolean) = {
    val r = mock[Random]
    r.nextBoolean() returns bool
    r
  }

  private def maxCounter = {
    val sim = new ErrorSimulator(freq(0, Duration.fromNanoseconds(0)))
    val res = sim.simulate(exception) {
      op
    }
    res must beEqualTo(op)
  }

  private def either[T](op: => T): Either[Throwable, T] = {
    try {
      Right(op)
    } catch {
      case t => Left(t)
    }
  }

  private def err = {
    val random = rand(true)
    val sim = new ErrorSimulator(freq(1, Duration.fromMilliseconds(1000)), rand = random)
    either(sim.simulate(exception)) must beLeft.like {
      case t => t.getMessage must beEqualTo(exception.getMessage)
    }
  }

  private def normal = {
    val random = rand(false)
    val sim = new ErrorSimulator(freq(1, Duration.fromMilliseconds(1000)), rand = random)
    either(sim.simulate(exception)) must beRight.like {
      case r => r must beEqualTo(op)
    }
  }

}
