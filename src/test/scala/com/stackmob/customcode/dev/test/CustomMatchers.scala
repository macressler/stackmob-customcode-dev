package com.stackmob.customcode.dev.test

import org.specs2.Specification
import scala.reflect.ClassTag
import org.specs2.matcher.MatchResult

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test
 *
 * User: aaron
 * Date: 4/30/13
 * Time: 5:23 PM
 */
trait CustomMatchers { this: Specification =>
  protected def beThrowable(expected: Throwable) = beLeft[Throwable].like {
    case t: Throwable => t must beEqualTo(expected)
  }

  protected def beThrowableInstance[T <: Throwable: ClassTag] = beLeft[Throwable].like {
    case t => t must beAnInstanceOf[T]
  }

  protected def beThrowableInstance[T <: Throwable: ClassTag, U](fn: Throwable => MatchResult[U]) = beLeft[Throwable].like {
    case t => {
      val instance = t must beAnInstanceOf[T]
      val res = fn(t)
      instance and res
    }
  }
}
