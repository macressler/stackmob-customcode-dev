package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.server.sdk.data.smValue
import collection.JavaConverters._
import scala.util.Try
import com.stackmob.core.DatastoreException

private[dataservice] trait AddRelated extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  protected case class AddRelated() extends BaseTestContext {
    private lazy val (_, smObj, datastore, svc) = defaults

    def correctParentSchema = {
      val primaryId = "objId"
      val relatedSchemaName = "children"
      val ret = svc.addRelatedObjects(schemaName, smValue(primaryId), relatedSchemaName, List(smValue("child1")).asJava)
      val returnedCorrectly = ret must beEqualTo(smObj)

      val schemaSplit = datastore.putCalls.get(0).schema.split("/").toList
      val schemaLengthRes = schemaSplit.length must beEqualTo(3)
      val schemaNameRes = schemaSplit(0) must beEqualTo(schemaName)
      val primaryIdRes = schemaSplit(1) must beEqualTo(primaryId)
      val relatedSchemaNameRes = schemaSplit(2) must beEqualTo(relatedSchemaName)

      returnedCorrectly and schemaLengthRes and schemaNameRes and primaryIdRes and relatedSchemaNameRes
    }

    def throwIfNoObjectIdSMString = {
      Try {
        svc.addRelatedObjects(schemaName, smValue(1), "b", List(smValue("c")).asJava)
      }.toEither must beThrowableInstance[DatastoreException]
    }

    def throwIfNoRelatedIdSMString = {
      Try {
        svc.addRelatedObjects(schemaName, smValue("a"), "b", List(smValue(1), smValue("2")).asJava)
      }.toEither must beThrowableInstance[DatastoreException]
    }
  }
}
