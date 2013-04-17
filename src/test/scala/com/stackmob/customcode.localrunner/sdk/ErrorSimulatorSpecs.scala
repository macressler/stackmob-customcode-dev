package com.stackmob.customcode.localrunner.sdk

import org.specs2.Specification
import com.twitter.util.{Time, Duration}
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
  "ErrorSimulator is a class to simulate N errors in a time window T"                                                   ^ end ^
  "upon initial creation, the simulator must have 0 count and 0 rollover"                                               ! getFunctions ^ end ^
  "if the error counter is at the maximum, run the operation normally"                                                  ! maxCounter ^ end ^
  "if the error counter is not at max, throw"                                                                           ! err ^ end ^
  "if the counter is not at max, execute normally"                                                                      ! normal ^ end ^
//  "rollovers must happen correctly"                                                                                     ! rollover ^ end ^
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
  private def either[T](op: => T): Either[Throwable, T] = {
    try {
      Right(op)
    } catch {
      case t: Throwable => Left(t)
    }
  }

  private def getFunctions = {
    val duration = Duration.fromNanoseconds(1000)
    val sim = new ErrorSimulator(freq(0, duration))
    val countRes = sim.getCount must beEqualTo(0)
    val rolloverRes = sim.getLastRollover must beLessThanOrEqualTo(Time.now)
    countRes and rolloverRes
  }

  private def maxCounter = {
    val sim = new ErrorSimulator(freq(0, Duration.fromNanoseconds(0)))
    val exRes = sim.simulate(exception)(op) must beEqualTo(op)
    val countRes = sim.getCount must beEqualTo(0)
    exRes and countRes
  }

  private def   err = {
    val random = rand(true)
    val sim = new ErrorSimulator(freq(100, Duration.fromMilliseconds(1000)), rand = random)
    //trigger a rollover
    either(sim.simulate(exception)(op))
    //then execute the real thing
    val exRes = either(sim.simulate(exception)(op)) must beLeft.like {
      case t => t.getMessage must beEqualTo(exception.getMessage)
    }

    exRes
  }

  private def normal = {
    val random = rand(false)
    val sim = new ErrorSimulator(freq(1, Duration.fromMilliseconds(1000)), rand = random)
    val exRes = either(sim.simulate(exception)(op)) must beRight.like {
      case r => r must beEqualTo(op)
    }

    exRes
  }

}
