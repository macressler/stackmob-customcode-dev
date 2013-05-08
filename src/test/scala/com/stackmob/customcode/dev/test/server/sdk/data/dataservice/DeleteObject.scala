package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import org.specs2.mock.Mockito
import scala.util.Try
import com.stackmob.customcode.dev.server.sdk.data.smValue
import com.stackmob.core.DatastoreException

private[dataservice] trait DeleteObject extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  protected case class DeleteObject() extends BaseTestContext {
    private val (_, _, datastore, svc) = defaults

    def worksOnSchema = {
      val objId = "a"
      val ret: Boolean = svc.deleteObject(schemaName, smValue(objId))
      val correctReturn = ret must beTrue
      val correctSchema = datastore.deleteCalls.get(0).schema must beEqualTo(s"$schemaName/$objId")
      correctReturn and correctSchema
    }

    def throwIfNoObjectIDSMString = {
      Try(svc.deleteObject(schemaName, smValue(1))).toEither must beThrowableInstance[DatastoreException]
    }
  }

}
