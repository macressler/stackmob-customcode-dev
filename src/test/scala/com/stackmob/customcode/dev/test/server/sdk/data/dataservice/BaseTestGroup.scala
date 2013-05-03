package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import com.stackmob.customcode.dev.test.CustomMatchers
import java.util.UUID
import com.stackmob.sdk.api.{StackMob, StackMobDatastore}
import com.stackmob.customcode.dev.server.sdk.data.DataServiceImpl._
import com.stackmob.customcode.dev.server.sdk.simulator.CallLimitation
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.sdkapi.SMObject
import com.stackmob.customcode.dev.test.server.sdk.data.{ResponseDetails, MockStackMobDatastore}
import scala.util.Try
import com.stackmob.core.{DatastoreException, InvalidSchemaException}
import com.stackmob.customcode.dev.server.json
import org.specs2.mock.Mockito

private [dataservice] trait BaseTestGroup { this: Specification with CustomMatchers with Mockito =>

  protected trait BaseTestContext {
    protected implicit lazy val session = UUID.randomUUID()
    protected def dataService(stackMobDatastore: StackMobDatastore,
                              maxCallsPerRequest: Int = DefaultMaxCallsPerRequest,
                              allCallsLimiter: CallLimitation = DefaultCallLimitation) = {
      val stackMob = {
        val s = mock[StackMob]
        s.getDatastore returns stackMobDatastore
        s
      }
      StackMob.setStackMob(new StackMob(0, "testAPIKey"))
      new DataServiceImpl(stackMob, maxCallsPerRequest, allCallsLimiter)
    }

    protected lazy val schemaName = "test-schema"
    protected lazy val invalidSchemaName = "test-invalid-schema"

    protected def defaults = {
      val defaultMap = Map("key1" -> "val1")
      val defaultSMObj = smObject(defaultMap)
      val defaultDatastore = new MockStackMobDatastore(new ResponseDetails(200, Nil, json.write(defaultMap).getBytes),
        new ResponseDetails(200, Nil, json.write(defaultMap).getBytes))
      val defaultDataservice = dataService(defaultDatastore)
      (defaultMap, defaultSMObj, defaultDatastore, defaultDataservice)
    }

    def commonErrors[ResType](fn: (DataServiceImpl, String, SMObject) => ResType,
                     obj: SMObject = defaults._2) = {
      val limited = {
        val ex = new Exception("hello world")
        val callLim = CallLimitation(0, _ => ex)
        val datastore = new MockStackMobDatastore(new ResponseDetails(200), new ResponseDetails(200))
        val svc = dataService(datastore, maxCallsPerRequest = 1000, allCallsLimiter = callLim)
        Try(fn(svc, schemaName, obj)).toEither must beThrowable(ex)
      }

      val maxCallsPerReq = {
        val datastore = new MockStackMobDatastore(new ResponseDetails(200), new ResponseDetails(200))
        val svc = dataService(datastore, maxCallsPerRequest = 0)
        //TODO: implement the calls per request limit. see https://github.com/stackmob/stackmob-customcode-localrunner/issues/29
        //Try(svc.createObject(schemaName, obj)).toEither must beThrowableInstance[CallsPerRequestLimitExceeded]
        svc must beAnInstanceOf[DataServiceImpl]
      }

      val invalidSchema = {
        val datastore = new MockStackMobDatastore(new ResponseDetails(400), new ResponseDetails(400))
        val svc = dataService(datastore)
        Try(fn(svc, invalidSchemaName, obj)).toEither must beThrowableInstance[InvalidSchemaException]
      }

      val otherErrCode = {
        val datastore = new MockStackMobDatastore(new ResponseDetails(200), new ResponseDetails(401))
        val svc = dataService(datastore)
        Try(fn(svc, schemaName, obj)).toEither must beThrowableInstance[DatastoreException]
      }

      limited and maxCallsPerReq and invalidSchema and otherErrCode
    }
  }

}
