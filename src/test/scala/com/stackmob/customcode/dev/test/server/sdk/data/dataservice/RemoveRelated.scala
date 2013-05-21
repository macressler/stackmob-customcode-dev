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

private[dataservice] trait RemoveRelated extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  protected case class RemoveRelated() extends BaseTestContext {
    private val (_, smObj, datastore, svc) = defaults

    def correctParentSchema = {
      val objId = "objId"
      val relation = "rel"
      val relatedIds = List("rel1", "rel2", "rel3")
      svc.removeRelatedObjects(schemaName, smValue(objId), relation, relatedIds.map(smValue(_)).asJava, true)
      datastore.deleteCalls.get(0).schema must beEqualTo(s"$schemaName/$objId/$relation")
    }

    def throwIfNoObjectIdSMString = {
      Try {
        svc.removeRelatedObjects(schemaName, smValue(1), "rel", List(smValue("a")).asJava, true)
      }.toEither must beThrowableInstance[DatastoreException]
    }

    def throwIfNoRelatedIDSMStrings = {
      Try {
        svc.removeRelatedObjects(schemaName, smValue("a"), "rel", List(smValue("a"), smValue(1)).asJava, true)
      }.toEither must beThrowableInstance[DatastoreException]
    }
  }

}
