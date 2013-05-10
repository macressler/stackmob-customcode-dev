package com.stackmob.customcode.dev

import com.stackmob.customcode.dev.server.sdk.{JavaMap, JavaList}
import collection.JavaConverters._
import java.util.concurrent.atomic.AtomicInteger
import com.stackmob.customcode.dev.server.sdk.JavaEnumeration
import com.stackmob.customcode.dev.server.sdk.simulator.{Frequency, ThrowableFrequency}
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test
 *
 * User: aaron
 * Date: 4/18/13
 * Time: 5:58 PM
 */
package object test {
  val throwableFreq0 = ThrowableFrequency(new Exception(""), Frequency(0, Duration(0, TimeUnit.SECONDS)))

  implicit class JavaEntryListW[T, U](l: JavaList[JavaMap.Entry[T, U]]) {
    def toTuples: List[(T, U)] = {
      l.asScala.map { entry =>
        entry.getKey -> entry.getValue
      }.toList
    }
  }

  implicit class ScalaTupleListW[T, U](l: List[(T, U)]) {
    def toEntries: JavaList[JavaMap.Entry[T, U]] = {
      l.map { tup =>
        val entry: JavaMap.Entry[T, U] = new JavaMap.Entry[T, U] {
          override lazy val getKey = tup._1
          override lazy val getValue = tup._2
          override def setValue(value: U) = getValue
        }
        entry
      }.toList.asJava
    }
  }

  /**
   * an enumeration over a list of elements
   * @param elements the elements over which to enumerate
   */
  class MockEnumeration[T](elements: Seq[T]) extends JavaEnumeration[T] {
    private lazy val idx = new AtomicInteger(0)

    override def hasMoreElements = idx.synchronized {
      idx.get < elements.length
    }

    override def nextElement: T = idx.synchronized {
      try {
        elements.apply(idx.getAndIncrement)
      } catch {
        case t: Throwable => {
          throw new NoSuchElementException()
        }
      }
    }
  }

  object MockEnumeration {
    def apply[T](seq: Seq[T]): MockEnumeration[T] = {
      new MockEnumeration[T](seq)
    }
  }
}
