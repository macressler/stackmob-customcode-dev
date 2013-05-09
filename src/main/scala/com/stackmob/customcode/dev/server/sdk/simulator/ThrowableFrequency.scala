package com.stackmob.customcode.dev.server.sdk.simulator

import com.twitter.util.Time
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

/**
* a single throwable and a frequency with which it should be thrown
* @param err the error to throw
* @param freq the frequency with which to throw it
* @param rand the random number generator to be used to decide when to throw
*/
class ThrowableFrequency(val err: Throwable,
                         val freq: Frequency,
                         val rand: Random = DefaultRandom) {
  private var count = 0
  private var lastRollover = Time.now
  private val lock = new Object
  def getCount = count
  def getLastRollover = lastRollover

  /**
   * simulate a call to op, randomly selecting when to throw based on freq
   * @tparam T the return type of op
   * @return the result of executing op (assuming this method didn't throw)
   */
  def simulate[T]() {
    lock.synchronized {
      if(lastRollover + freq.every <= Time.now) {
        //if a rollover happened, reset stuff
        lastRollover = Time.now
        count = 0
      } else if(count >= freq.number) {
        //if the counter is at max, run the op normally
        count += 1
      } else {
        //if the counter is not at max, randomly decide if there's an error
        val shouldErr = rand.nextBoolean()
        if(shouldErr) {
          count += 1
          throw err
        }
      }
    }
  }
}

object ThrowableFrequency {
  def apply(err: Throwable, freq: Frequency, rand: Random = DefaultRandom) = {
    new ThrowableFrequency(err, freq, rand)
  }
}
