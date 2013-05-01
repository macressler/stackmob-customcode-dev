package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import com.stackmob.customcode.dev.test.CustomMatchers
import org.specs2.specification.Fragments
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.test.server.sdk.data.{ResponseDetails, MockStackMobDatastore}
import com.stackmob.customcode.dev.server.json
import com.stackmob.customcode.dev.server.sdk.data.extensions._

trait CreateObjectFragments extends BaseFragments { this: Specification with CustomMatchers =>
  protected lazy val createObjectFragments: Fragments = {
    "createObject should"                                                                                               ^
      "create the proper schema"                                                                                        ! CreateObject().correctSchema ^
      "convert the response to an SMObject correctly"                                                                   ! CreateObject().correctResponse ^
      "handle common errors properly"                                                                                   ! CreateObject().commonErrors() ^
      end
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
