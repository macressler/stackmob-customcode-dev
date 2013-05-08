package com.stackmob.customcode.dev.server

import com.stackmob.sdk.callback.StackMobCallback
import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import com.stackmob.sdk.exception.StackMobException
import scalaz.{Validation, Failure, Success}
import scalaz.Scalaz._
import scalaz.concurrent.Promise
import java.util.Map

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.sdk
 *
 * User: aaron
 * Date: 3/28/13
 * Time: 4:45 PM
 */
package object sdk {
  val DefaultTimeoutException = new StackMobException("datastore didn't return in time")
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
        mbPolled <- q.poll(1000, TimeUnit.SECONDS)
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
      override lazy val getKey = key
      override lazy val getValue = value
      override def setValue(v: Y) = value
    }
    def entry[X, Y](tup: (X, Y)): Entry[X, Y] = {
      entry(tup._1, tup._2)
    }
  }
  implicit class EntryW[T, U](e: java.util.Map.Entry[T, U]) {
    def tup = e.getKey -> e.getValue
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
}
