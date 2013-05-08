package com.stackmob.customcode.dev

import scala.util.{Try, Success, Failure}
import com.stackmob.customcode.dev.server.sdk.{JavaMap, JavaList}
import collection.JavaConverters._

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
}
