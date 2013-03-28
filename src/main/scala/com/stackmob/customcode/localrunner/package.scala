package com.stackmob.customcode

import scalaz.Validation
import scalaz.Scalaz._

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
  val appName = "cc-localrunner-app"
  val loggedInUser = "cc-localrunner-logged-in-user"
  val userSchemaName = "cc-localrunner-user-schema"

  def validating[T](t: => T): Validation[Throwable, T] = {
    try {
      t.success[Throwable]
    } catch {
      case t => t.fail[T]
    }
  }
}
