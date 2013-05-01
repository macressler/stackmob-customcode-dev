package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import com.stackmob.customcode.dev.test.CustomMatchers
import java.util.UUID
import com.stackmob.sdk.api.StackMobDatastore
import com.stackmob.customcode.dev.server.sdk.data.DataServiceImpl._
import com.stackmob.customcode.dev.server.sdk.simulator.CallLimitation
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.sdkapi.SMObject
import com.stackmob.customcode.dev.test.server.sdk.data.{ResponseDetails, MockStackMobDatastore}
import scala.util.Try
import com.stackmob.core.{DatastoreException, InvalidSchemaException}

private [dataservice] trait BaseFragments { this: Specification with CustomMatchers =>
  protected sealed trait Base {
    protected implicit lazy val session = UUID.randomUUID()
    protected def dataService(smDatastore: StackMobDatastore,
                              maxCallsPerRequest: Int = DefaultMaxCallsPerRequest,
                              allCallsLimiter: CallLimitation = DefaultCallLimitation) = {
      new DataServiceImpl(smDatastore, maxCallsPerRequest, allCallsLimiter)
    }

    protected lazy val schemaName = "test-schema"

    lazy val defaultSMObject = smObject(Map("a" -> "b"))

    def commonErrors(obj: SMObject = defaultSMObject) = {
      val limited = {
        val ex = new Exception("hello world")
        val callLim = CallLimitation(0, _ => ex)
        val datastore = new MockStackMobDatastore(new ResponseDetails(200))
        val svc = dataService(datastore, maxCallsPerRequest = 1000, allCallsLimiter = callLim)
        Try(svc.createObject(schemaName, obj)).toEither must beThrowable(ex)
      }

      val maxCallsPerReq = {
        val datastore = new MockStackMobDatastore(new ResponseDetails(200))
        val svc = dataService(datastore, maxCallsPerRequest = 0)
        //TODO: implement the calls per request limit
        //Try(svc.createObject(schemaName, obj)).toEither must beThrowableInstance[CallsPerRequestLimitExceeded]
        svc must beAnInstanceOf[DataServiceImpl]
      }

      val invalidSchema = {
        val datastore = new MockStackMobDatastore(new ResponseDetails(400))
        val svc = dataService(datastore)
        Try(svc.createObject("invalid-schema", obj)).toEither must beThrowableInstance[InvalidSchemaException]
      }

      val otherErrCode = {
        val datastore = new MockStackMobDatastore(new ResponseDetails(401))
        val svc = dataService(datastore)
        Try(svc.createObject(schemaName, obj)).toEither must beThrowableInstance[DatastoreException]
      }

      limited and maxCallsPerReq and invalidSchema and otherErrCode
    }
  }

}
