package com.stackmob.customcode.localrunner

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner
 *
 * User: aaron
 * Date: 4/18/13
 * Time: 2:42 PM
 */
object TestUtils {
  def either[T](op: => T): Either[Throwable, T] = {
    try {
      Right(op)
    } catch {
      case t: Throwable => {
        Left(t)
      }
    }
  }

}
