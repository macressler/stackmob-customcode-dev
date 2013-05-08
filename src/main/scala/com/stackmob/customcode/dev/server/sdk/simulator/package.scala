package com.stackmob.customcode.dev.server.sdk

import com.twitter.util.Duration
import scala.util.Random

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.sdk.simulator
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 2:42 PM
 */
package object simulator {
  def DefaultRandom = new Random(System.currentTimeMillis())

  class Frequency(val number: Int, val every: Duration)
  object Frequency {
    def apply(number: Int, every: Duration) = {
      new Frequency(number, every)
    }
  }

}
