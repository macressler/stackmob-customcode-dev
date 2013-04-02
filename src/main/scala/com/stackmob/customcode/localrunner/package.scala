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

  def appName = "cc-localrunner-app-%s".format(uuid)
  def loggedInUser = "cc-localrunner-logged-in-user-%s".format(uuid)
  def testFBUser = "cc-localrunner-test-facebook-user-%s".format(uuid)
  def testFBMessageID = "cc-localrunner-test-facebook-message-id-%s".format(uuid)
  def userSchemaName = "cc-localrunner-user-schema-%s".format(uuid)

  def validating[T](t: => T): Validation[Throwable, T] = {
    try {
      t.success[Throwable]
    } catch {
      case t => t.fail[T]
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
