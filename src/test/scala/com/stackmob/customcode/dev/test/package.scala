package com.stackmob.customcode.dev

import scala.util.{Try, Success, Failure}

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
  implicit class TryW[T](t: Try[T]) {
    def toEither: Either[Throwable, T] = {
      t match {
        case Success(successVal) => Right(successVal)
        case Failure(throwable) => Left(throwable)
      }
    }
  }
}
