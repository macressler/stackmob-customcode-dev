package com.stackmob.customcode.dev

import scalaz.{Validation, Success, Failure}
import scalaz.Scalaz._
import java.util.UUID
import java.io.BufferedReader

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner
 *
 * User: aaron
 * Date: 3/27/13
 * Time: 4:53 PM
 */
package object localrunner {
  lazy val maxDepth = 2
  class DepthLimitReached(depth: Int) extends Exception("the maximum depth of %d has been reached".format(depth))
  object DepthLimitReached {
    def apply(depth: Int) = {
      new DepthLimitReached(depth)
    }
  }

  lazy val uuid = UUID.randomUUID()

  private def fmt(s: String) = "cc-localrunner-%s-%s".format(s, uuid)

  lazy val appName = fmt("app")
  lazy val userSchemaName = fmt("user-schema")
  lazy val userName = fmt("user")
  lazy val loggedInUser = fmt("logged-in-user")
  lazy val testFBUser = fmt("facebook-user")
  lazy val testFBMessageID = fmt("facebook-message")
  lazy val testTwitterUser = fmt("twitter-user")

  def validating[T](t: => T): Validation[Throwable, T] = {
    try {
      t.success[Throwable]
    } catch {
      case t: Throwable => t.fail[T]
    }
  }

  implicit class ValidationW[Fail, Success](validation: Validation[Fail, Success]) {

    def mapFailure[NewFail](fn: Fail => NewFail): Validation[NewFail, Success] = {
      validation match {
        case Success(s) => Success(s)
        case Failure(t) => Failure(fn(t))
      }
    }
  }

  implicit class ThrowableValidationW[Success](validation: Validation[Throwable, Success]) {
    def getOrThrow: Success = {
      validation ||| { t: Throwable =>
        throw t
      }
    }
  }

  implicit class BufferedReaderW(val reader: BufferedReader) {
    def exhaust(builder: StringBuilder = new StringBuilder): StringBuilder = {
      Option(reader.readLine()).map { line =>
        exhaust(builder.append(line))
      }.getOrElse(builder)
    }
  }
}
