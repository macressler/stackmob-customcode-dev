package com.stackmob.customcode.dev.test
package server
package sdk
package data

import org.specs2.Specification
import com.stackmob.sdk.api.StackMobDatastore
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.server.json
import java.util.UUID
import com.stackmob.customcode.dev.server.sdk.simulator.CallLimitation
import com.stackmob.customcode.dev.server.sdk.data.extensions._
import scala.util.Try
import DataServiceImpl._
import com.stackmob.sdkapi.SMObject
import com.stackmob.core.{DatastoreException, InvalidSchemaException}

class DataServiceImplSpecs extends Specification with Mockito with CustomMatchers { def is =
  "DataServiceImplSpecs".title                                                                                          ^ end ^
  "DataService is the primary API for custom code to talk to the StackMob datastore"                                    ^ end ^
  "createObject should"                                                                                                 ^
    "create the proper schema"                                                                                          ! CreateObject().correctSchema ^
    "convert the response to an SMObject correctly"                                                                     ! CreateObject().correctResponse ^
    "handle common errors properly"                                                                                     ! CreateObject().commonErrors() ^
                                                                                                                        end ^
  "createRelatedObjects should"                                                                                         ^
    "throw if the given objectId isn't an SMString"                                                                     ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "readObjects should"                                                                                                  ^
    "read all objects correctly"                                                                                        ! pending ^
    "read all objects given conditions"                                                                                 ! pending ^
    "read only the requested fields"                                                                                    ! pending ^
    "read to the given expand depth properly"                                                                           ! pending ^
    "throw when the max expand depth is reached"                                                                        ! pending ^
    "honor all result filters properly"                                                                                 ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "updateObject should"                                                                                                 ^
    "work on the proper schema and ID"                                                                                  ! pending ^
    "apply the right update actions"                                                                                    ! pending ^
    "only work when conditions are met"                                                                                 ! pending ^
    "throw if an SMString wasn't given"                                                                                 ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "updateObjects should"                                                                                                ^
    "work on the right schema"                                                                                          ! pending ^
    "only work when conditions are met"                                                                                 ! pending ^
    "apply the right update actions"                                                                                    ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "addRelatedObjects should"                                                                                            ^
    "work on the correct parent schema"                                                                                 ! pending ^
    "throw if an SMString wasn't given for the object ID"                                                               ! pending ^
    "throw if any of the related IDs aren't SMStrings"                                                                  ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "deleteObject should"                                                                                                 ^
    "operate on the correct schema"                                                                                     ! pending ^
    "throw if an SMString wasn't given for the object ID"                                                               ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "removeRelatedObjects should"                                                                                         ! pending ^
    "operate on the correct parent schema"                                                                              ! pending ^
    "throw if the given object id isn't an SMString"                                                                    ! pending ^
    "throw if any of the given related IDs aren't SMStrings"                                                            ! pending ^
    "honor the cascadeDelete flag"                                                                                      ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "countObjects should"                                                                                                 ^
    "operate on the correct schema"                                                                                     ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "getObjectModelNames should"                                                                                          ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end




  private sealed trait Base {
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

  private case class CreateObject() extends Base {
    val map = Map("key1" -> "val1")
    val obj = smObject(map)
    val datastore = new MockStackMobDatastore(new ResponseDetails(200, Nil, json.write(map).getBytes))

    def correctSchema = {
      val res = dataService(datastore).createObject(schemaName, obj)
      val called = datastore.numPostCalls must beEqualTo(1)
      val returnCorrect = res must beEqualTo(obj)
      val correctSchema = datastore.postCalls.get(0).schema must beEqualTo(schemaName)
      called and returnCorrect and correctSchema
    }

    def correctResponse = {
      val createRes = dataService(datastore).createObject(schemaName, obj)
      createRes.toObjectMap() must beEqualTo(map)
    }
  }

}
