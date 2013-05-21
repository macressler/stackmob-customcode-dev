package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import com.stackmob.customcode.dev.test.CustomMatchers
import org.specs2.mock.Mockito
import scala.util.Try
import com.stackmob.core.DatastoreException

trait ObjectModelNames extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  protected case class ObjectModelNames() extends BaseTestContext {
    private val (_, _, _, svc) = defaults
    def throws = {
      Try(svc.getObjectModelNames).toEither must beThrowableInstance[DatastoreException, String] { t =>
        t.getMessage must contain("not yet implemented")
      }
    }
  }

}
