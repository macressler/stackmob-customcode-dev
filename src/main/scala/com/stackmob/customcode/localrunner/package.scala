package com.stackmob.customcode

import scalaz.{Validation, Success, Failure}
import scalaz.Scalaz._
import java.util.UUID

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

  sealed trait ValidationW[Fail, Success] {
    protected def validation: Validation[Fail, Success]

    def mapFailure[NewFail](fn: Fail => NewFail): Validation[NewFail, Success] = {
      validation match {
        case Success(s) => Success(s)
        case Failure(t) => Failure(fn(t))
      }
    }
  }
  implicit def validationToW[Fail, Success](v: Validation[Fail, Success]) = new ValidationW[Fail, Success] {
    override protected lazy val validation = v
  }

  sealed trait ThrowableValidationW[Success] {
    protected def validation: Validation[Throwable, Success]

    def getOrThrow: Success = {
      validation ||| { t: Throwable =>
        throw t
      }
    }
  }
  implicit def throwableValidationToW[Success](v: Validation[Throwable, Success]) = new ThrowableValidationW[Success] {
    override protected lazy val validation = v
  }
}
