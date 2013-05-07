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
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.server.json

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

    private val (_, obj, datastore, svc) = defaults
		private val id = "testId"
			
		def throwIfNoSMString = {
		  Try(svc.updateObject(schemaName, new SMBoolean(true), List[SMUpdate]().asJava)).toEither must beThrowableInstance[DatastoreException, String] { t: Throwable =>
        t.getMessage must beEqualTo("the given SMValue must be an SMString")
      }
    }
			
    def properSchemaAndId = {
      val updateRes = svc.updateObject(schemaName, id, List[SMUpdate]().asJava) must beEqualTo(obj)
      val reqSchema = datastore.putCalls.get(0).schema
      val reqSchemaSplit = reqSchema.split("/").toList

      val sizeRes = reqSchemaSplit.length must beEqualTo(2)
      val schemaRes = reqSchemaSplit(0) must beEqualTo(schemaName)
      val idRes = reqSchemaSplit(1) must beEqualTo(id)
				
      updateRes and sizeRes and schemaRes and idRes
    }
			
    def appliesRightupdates = {
      val smInt = new SMInt(1)
      val smSet = new SMSet("field", smInt)
      val smInc = new SMIncrement("field", smInt)

      val smUpdateList = List(smSet, smInc)

      svc.updateObject(schemaName, "id", smUpdateList.asJava)
      val expectedBody = json.write(smBody(smUpdateList))
      val resultantBody = datastore.putCalls.get(0).body
      val putRes = resultantBody must beSome.like {
        case b => {
          b must beEqualTo(expectedBody)
        }
      }
      putRes
    }
      }


}
