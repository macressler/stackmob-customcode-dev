package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.{ScalaCheck, Specification}
import com.stackmob.customcode.dev.test.CustomMatchers
import org.specs2.mock.Mockito
import collection.JavaConverters._
import com.stackmob.sdkapi._
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.server.json
import com.stackmob.customcode.dev.server.sdk.EntryW
import scala.util.Try
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen

trait ReadObjects extends BaseTestGroup { this: Specification with CustomMatchers with Mockito with ScalaCheck =>
  case class ReadObjects() extends BaseTestContext {
    override protected def defaults = {
      val (origMap, origObj, _, _) = super.defaults
      val ds = new MockStackMobDatastore(new ResponseDetails(200, body = json.write(List(origMap)).getBytesUTF8),
        ResponseDetails(200),
        ResponseDetails(200),
        ResponseDetails(200)
      )
      val svc = dataService(ds)
      (origMap, origObj, ds, svc)
    }

    def readsAllCorrectly = {
      val (map, _, _, svc) = defaults
      val res = svc.readObjects(schemaName, List[SMCondition]().asJava).asScala
      val expectedSMObjectList = smObjectList(map :: Nil)
      res must haveTheSameElementsAs(expectedSMObjectList)
    }
    def readsGivenConditions = {
      val (_, _, ds, svc) = defaults
      val field = "field"
      val valueStr = "value"
      val valueSM = smValue(valueStr)
      //TODO: include geo operations and AND/OR
      val allConditions = new SMEquals(field, valueSM) ::
        new SMGreater(field, valueSM) ::
        new SMGreaterOrEqual(field, valueSM) ::
        new SMIn(field, List(valueSM).asJava) ::
        new SMIsNull(field, new SMBoolean(true)) ::
        new SMLess(field, valueSM) ::
        new SMLessOrEqual(field, valueSM) ::
        new SMNotEqual(field, valueSM) ::
      Nil

      val expectedQuery = smQuery(schemaName, allConditions)
      svc.readObjects(schemaName, allConditions.asJava)
      ds.getCalls.get(0).queryStringParams.toMap must haveTheSameElementsAs(expectedQuery.getArguments.asScala.map(_.tup).toMap)
    }
    def requestedFields = {
      val (_, _, ds, svc) = defaults
      val fields = List("field1")
      svc.readObjects(schemaName, List[SMCondition]().asJava, fields.asJava)
      val expectedOptions = smOptions(1, Some(fields))
      ds.getCalls.get(0).headers must haveTheSameElementsAs(expectedOptions.getHeaders.asScala.map(_.tup))
    }
    def expandDepth = {
      val (_, _, ds, svc) = defaults
      val expandDepth = 2
      svc.readObjects(schemaName, List[SMCondition]().asJava, expandDepth)
      val expectedOptions = smOptions(expandDepth)
      ds.getCalls.get(0).headers must haveTheSameElementsAs(expectedOptions.getHeaders.asScala.map(_.tup))
    }
    def throwForHighExpandDepth = forAll(Gen.choose(4, Int.MaxValue)) { depth =>
      val (_, _, _, svc) = defaults
      Try(svc.readObjects(schemaName, List[SMCondition]().asJava, depth)).toEither must beThrowableInstance[IllegalArgumentException]
    }
  }
}
