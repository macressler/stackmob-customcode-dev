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

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CopyOnWriteArrayList
import collection.JavaConverters._

/**
 * a class to limit the number of calls to an operation, and throw if the limit has been reached
 * @param limit the max number of calls to allow
 * @param ex a function that takes all the call names that were executed before the limit, and return an exception to
 *           throw now that we're over the limit
 */
class CallLimitation(val limit: Int, val ex: Seq[String] => Throwable) {
  private val count = new AtomicInteger(0)
  private val calls = new CopyOnWriteArrayList[String]()

  /**
   * execute this limitation
   * @param op the operation to execute. will be executed exactly once if no limitations were met, else 0 times
   * @tparam T the return type of op
   * @return the result of executing op exactly once
   */
  def apply[T](name: String)(op: => T): T = {
    calls.add(name)
    if(count.incrementAndGet() >= limit) {
      val list = calls.subList(0, calls.size())
      throw ex(list.asScala)
    } else {
      op
    }
  }
}

object CallLimitation {
  def apply(limit: Int)(ex: Seq[String] => Throwable): CallLimitation = {
    new CallLimitation(limit, ex)
  }
}
