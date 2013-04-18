package com.stackmob.customcode.dev.localrunner

import com.stackmob.sdk.callback.StackMobCallback
import scalaz.concurrent.Promise
import java.util.concurrent.LinkedBlockingQueue
import com.stackmob.sdk.exception.StackMobException
import scalaz.{Validation, Failure, Success}
import scalaz.concurrent.Promise

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk
 *
 * User: aaron
 * Date: 3/28/13
 * Time: 4:45 PM
 */
package object sdk {

  def synchronous(fn: StackMobCallback => Unit): Promise[Validation[StackMobException, String]] = {
    val q = new LinkedBlockingQueue[Validation[StackMobException, String]](1)
    val callback = new StackMobCallback {
      def failure(e: StackMobException) {
        q.put(Failure(e))
      }
      def success(responseBody: String) {
        q.put(Success(responseBody))
      }
    }
    Promise(fn(callback))
    Promise(q.take)
  }

  type JList[T] = java.util.List[T]
  type JArrayList[T] = java.util.ArrayList[T]

  type JMap[K, V] = java.util.Map[K, V]
  type JHashMap[K, V] = java.util.HashMap[K, V]
  object JMap {
    def apply[K, V](pairs: (K, V)*): JMap[K, V] = {
      val m = new JHashMap[K, V]
      pairs.toList.foreach { tup =>
        m.put(tup._1, tup._2)
      }
      m
    }
  }

  type JBoolean = java.lang.Boolean

  type JLong = java.lang.Long

  type JSet[T] = java.util.Set[T]
  type JHashSet[T] = java.util.HashSet[T]
  object JSet {
    def apply[T](elts: T*): JSet[T] = {
      val s = new JHashSet[T]
      elts.toList.foreach { elt =>
        s.add(elt)
      }
      s
    }
  }



}
