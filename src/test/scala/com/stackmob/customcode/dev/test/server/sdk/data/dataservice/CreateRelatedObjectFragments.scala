package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import scala.util.Try
import com.stackmob.sdkapi._
import collection.JavaConverters._
import com.stackmob.core.DatastoreException
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.server.json
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.server.sdk.data.DataServiceImpl.PostRelatedResponse

private [dataservice] trait CreateRelatedObjectFragments
  extends BaseFragments { this: Specification with CustomMatchers with Mockito =>

  protected case class CreateRelatedObjects() extends Base {
    private lazy val (_, _, _, svc) = defaults
    private lazy val relatedObjectsToCreate = List(smObject(Map("hello" -> "world"))).asJava
    def throwNotSMString = {
      Try {
        svc.createRelatedObjects(schemaName, new SMInt(1), "related", relatedObjectsToCreate)
      }.toEither must beThrowableInstance[DatastoreException]
    }

    def decodeResult = {
      val successIdList = List(1.toString, 2.toString, 3.toString)
      val failIdList = List(4.toString, 5.toString, 6.toString)
      val bulkRes = PostRelatedResponse(successIdList, failIdList)
      val datastore = new MockStackMobDatastore(new ResponseDetails(200, body = json.write(bulkRes).getBytes))
      val svc = dataService(datastore)
      val bulkResult = svc.createRelatedObjects(schemaName, new SMString("helloworld"), "related", relatedObjectsToCreate)

      val successSMValueList = successIdList.map(smValue(_))
      val failSMValueList = failIdList.map(smValue(_))

      val successMatches = bulkResult.getSuccessIds.asScala must haveTheSameElementsAs(successSMValueList)
      val failMatches = bulkResult.getFailedIds.asScala must haveTheSameElementsAs(failSMValueList)

      successMatches and failMatches
    }
  }
}
