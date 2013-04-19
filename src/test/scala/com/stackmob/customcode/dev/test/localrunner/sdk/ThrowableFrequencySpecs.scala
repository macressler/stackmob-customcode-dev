package com.stackmob.customcode.dev
package test
package localrunner
package sdk

import org.specs2.Specification
import com.twitter.util.{Time, Duration}
import org.specs2.mock.Mockito
import scala.util.Random
import com.stackmob.customcode.dev.localrunner.sdk.simulator.{ThrowableFrequency, Frequency}
import scala.util.Try

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 11:15 AM
 */
class ThrowableFrequencySpecs extends Specification with Mockito { def is =
  "ErrorSimulator".title                                                                                                ^
  "ErrorSimulator is a class to simulate N errors in a time window T"                                                   ^ end ^
  "upon initial creation, the simulator must have 0 count and 0 rollover"                                               ! getFunctions ^ end ^
  "if the error counter is at the maximum, run the operation normally"                                                  ! maxCounter ^ end ^
  "if the error counter is not at max, throw"                                                                           ! err ^ end ^
  "if the counter is not at max, execute normally"                                                                      ! normal ^ end ^
                                                                                                                        end
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

  private def getFunctions = {
    val duration = Duration.fromNanoseconds(1000)
    val freq = ThrowableFrequency(exception, Frequency(0, duration))
    val countRes = freq.getCount must beEqualTo(0)
    val rolloverRes = freq.getLastRollover must beLessThanOrEqualTo(Time.now)
    countRes and rolloverRes
  }

  private def maxCounter = {
    val duration = Duration.fromNanoseconds(0)
    val freq = ThrowableFrequency(exception, Frequency(0, duration))
    val exRes = Try(freq.simulate(op)).toEither must beRight.like {
      case r => r must beEqualTo(op)
    }
    val countRes = freq.getCount must beEqualTo(0)
    exRes and countRes
  }

  private def err = {
    val random = rand(true)
    val duration = Duration.fromMilliseconds(100)
    val freq = ThrowableFrequency(exception, Frequency(100, duration), rand = random)
    //trigger a rollover
    Try(freq.simulate(op))
    //then execute the real thing
    val exRes = Try(freq.simulate(op)).toEither must beLeft.like {
      case t => t.getMessage must beEqualTo(exception.getMessage)
    }

    exRes
  }

  private def normal = {
    val random = rand(false)
    val duration = Duration.fromMilliseconds(1000)
    val freq = ThrowableFrequency(exception, Frequency(1, duration), rand = random)
    val exRes = Try(freq.simulate(op)).toEither must beRight.like {
      case r => r must beEqualTo(op)
    }

    exRes
  }

}
