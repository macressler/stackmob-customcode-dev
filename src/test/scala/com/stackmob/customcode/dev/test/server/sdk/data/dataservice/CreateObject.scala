package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import com.stackmob.customcode.dev.test.CustomMatchers

import com.stackmob.customcode.dev.server.sdk.data.extensions._
import org.specs2.mock.Mockito

private[dataservice] trait CreateObject
  extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>

  protected case class CreateObject() extends BaseTestContext {
    private val (map, obj, datastore, svc) = defaults

    def correctSchema = {
      val res = svc.createObject(schemaName, obj)
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
