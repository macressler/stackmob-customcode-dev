package com.stackmob.customcode.dev.test

import org.specs2.Specification

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

  protected def beAThrowableLike[T <: Throwable] = beLeft[Throwable].like {
    case t => t must beAnInstanceOf[T]
  }
}
