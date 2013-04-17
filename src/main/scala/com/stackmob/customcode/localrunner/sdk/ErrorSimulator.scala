package com.stackmob.customcode.localrunner.sdk

import scala.util.Random
import com.twitter.util.{Time, Duration}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk
 *
 * User: aaron
 * Date: 4/15/13
 * Time: 5:16 PM
 */

class Frequency(val number: Int, val every: Duration)

/**
 * this class simulates errors given a specific frequency
 * @param freq the frequency which to trigger errors
 * @param rand the implementation of random. defaults to the built in Scala implementation
 */
class ErrorSimulator(freq: Frequency,
                     rand: Random = new Random(System.currentTimeMillis())) {
  private var count = 0
  private var lastRollover = Time.fromMilliseconds(0)
  private val lock = new Object

  def getCount = {
    count
  }

  def getLastRollover = {
    lastRollover
  }

  def simulate[T](err: Throwable)(op: => T): T = {
    lock.synchronized {
      if(lastRollover + freq.every <= Time.now) {
        //if a rollover happened, reset stuff
        lastRollover = Time.now
        count = 0
        op
      } else if(count >= freq.number) {
        //if the counter is at max, run the op normally
        count += 1
        op
      } else {
        //if the counter is not at max, randomly decide if there's an error
        val shouldErr = rand.nextBoolean()
        if(shouldErr) {
          count += 1
          throw err
        } else {
          op
        }
      }
    }
  }
}
