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

package com.stackmob.customcode.dev.server

import com.stackmob.sdk.callback.StackMobCallback
import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import com.stackmob.sdk.exception.StackMobException
import scalaz.{Validation, Failure, Success}
import scalaz.Scalaz._
import scalaz.concurrent.Promise
import java.util.Map

package object sdk {
  val DefaultTimeoutException = new StackMobException("datastore didn't return in time")
  private val synchronousPollTimeMS = 1000
  def synchronous[ResType](fn: StackMobCallback => ResType)
                          (implicit timeoutException: StackMobException = DefaultTimeoutException): Promise[Validation[StackMobException, String]] = {
    val q = new LinkedBlockingQueue[Validation[StackMobException, String]](1)
    val callback = new StackMobCallback {
      override def failure(e: StackMobException) {
        q.put(Failure(e))
      }
      override def success(responseBody: String) {
        q.put(Success(responseBody))
      }
    }
    Promise {
      fn(callback)
    }.map { _: ResType =>
      for {
        mbPolled <- q.poll(synchronousPollTimeMS, TimeUnit.SECONDS)
        polled <- Option(mbPolled).toSuccess {
          timeoutException
        }
      } yield {
        polled
      }
    }
  }

  type JavaList[T] = java.util.List[T]
  type JavaArrayList[T] = java.util.ArrayList[T]

  type JavaMap[K, V] = java.util.Map[K, V]
  type JavaHashMap[K, V] = java.util.HashMap[K, V]
  object JavaMap {
    def apply[K, V](pairs: (K, V)*): JavaMap[K, V] = {
      val m = new JavaHashMap[K, V]
      pairs.toList.foreach { tup =>
        m.put(tup._1, tup._2)
      }
      m
    }
    type Entry[X, Y] = java.util.Map.Entry[X, Y]
    def entry[X, Y](key: X, value: Y): Entry[X, Y] = new java.util.Map.Entry[X, Y] {
      override lazy val getKey: X = key
      override lazy val getValue: Y = value
      override def setValue(v: Y): Y = value
    }
    def entry[X, Y](tup: (X, Y)): Entry[X, Y] = {
      entry(tup._1, tup._2)
    }
  }
  implicit class EntryW[T, U](e: java.util.Map.Entry[T, U]) {
    def tup: (T, U) = e.getKey -> e.getValue
  }

  type JavaBoolean = java.lang.Boolean
  type JavaLong = java.lang.Long
  type JavaDouble = java.lang.Double

  type JavaSet[T] = java.util.Set[T]
  type JavaHashSet[T] = java.util.HashSet[T]
  object JavaSet {
    def apply[T](elements: T*): JavaSet[T] = {
      val s = new JavaHashSet[T]
      elements.toList.foreach { elt =>
        s.add(elt)
      }
      s
    }
  }

  type JavaTimeoutException = java.util.concurrent.TimeoutException
  type JavaEnumeration[T] = java.util.Enumeration[T]
}
