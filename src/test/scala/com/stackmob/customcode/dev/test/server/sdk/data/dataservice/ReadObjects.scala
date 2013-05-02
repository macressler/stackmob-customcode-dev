package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import com.stackmob.customcode.dev.test.CustomMatchers
import org.specs2.mock.Mockito
import collection.JavaConverters._
import com.stackmob.sdkapi.SMCondition
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.server.json

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data.dataservice
 *
 * User: aaron
 * Date: 5/1/13
 * Time: 6:19 PM
 */
trait ReadObjects extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  case class ReadObjects() extends BaseTestContext {
    def readsAllCorrectly = {
      val (map, _, _, _) = defaults
      val encodedBody = List(map)
      val datastore = new MockStackMobDatastore(new ResponseDetails(200, body = json.write(encodedBody).getBytes), new ResponseDetails(200))
      val svc = dataService(datastore)
      val res = svc.readObjects(schemaName, List[SMCondition]().asJava).asScala
      val expectedSMObjectList = smObjectList(map :: Nil)
      res must haveTheSameElementsAs(expectedSMObjectList)
    }
  }
}
