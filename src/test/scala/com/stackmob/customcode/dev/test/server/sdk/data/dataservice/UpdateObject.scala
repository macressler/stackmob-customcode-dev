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
import collection.JavaConverters._
import com.stackmob.sdkapi._
import com.stackmob.core.DatastoreException

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data.dataservice
 *
 * User: aaron
 * Date: 5/6/13
 * Time: 12:19 PM
 */
private[dataservice] trait UpdateObject
  extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>

  protected case class UpdateObject() extends BaseTestContext {
    private val (map, obj, datastore, svc) = defaults

    def throwIfNoSMString = {
      Try(svc.updateObject(schemaName, new SMBoolean(true), List[SMUpdate]().asJava)).toEither must beThrowableInstance[DatastoreException, String] { t: Throwable =>
        t.getMessage must beEqualTo("the given SMValue must be an SMString")
      }
    }
  }


}
