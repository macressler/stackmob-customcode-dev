/**
 * Copyright 2011-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.dev.server.sdk.simulator

import com.twitter.util.Time
import scala.util.Random

/**
* a single throwable and a frequency with which it should be thrown
* @param err the error to throw
* @param freq the frequency with which to throw it
* @param rand the random number generator to be used to decide when to throw
*/
class ThrowableFrequency(val err: Throwable,
                         val freq: Frequency,
                         val rand: Random = defaultRandom) {
  private var count = 0
  private var lastRollover = Time.now
  private val lock = new Object
  def getCount: Int = count
  def getLastRollover: Time = lastRollover

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
  def apply(err: Throwable, freq: Frequency, rand: Random = defaultRandom): ThrowableFrequency = {
    new ThrowableFrequency(err, freq, rand)
  }
}
