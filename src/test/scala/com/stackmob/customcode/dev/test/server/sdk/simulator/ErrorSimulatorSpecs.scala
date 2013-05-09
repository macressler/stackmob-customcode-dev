package com.stackmob.customcode.dev
package test
package server
package sdk
package simulator

import org.specs2.{ScalaCheck, Specification}
import org.specs2.mock.Mockito

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.sdk
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 4:02 PM
 */
class ErrorSimulatorSpecs extends Specification with CustomMatchers with Mockito with ScalaCheck { def is =
  "ErrorSimulatorSpecs".title                                                                                           ^ end ^
  end
  //TODO: ensure that an operation is only executed once in all cases

}
