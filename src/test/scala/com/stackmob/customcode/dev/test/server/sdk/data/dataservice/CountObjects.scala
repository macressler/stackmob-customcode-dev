package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import org.specs2.mock.Mockito

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data.dataservice
 *
 * User: aaron
 * Date: 5/6/13
 * Time: 2:13 PM
 */
trait CountObjects extends BaseTestGroup { this: Specification with Mockito with CustomMatchers =>

  case class CountObjects() extends BaseTestContext {
    private val count = 1L
    override protected lazy val defaults = {
      val getResponse = new ResponseDetails(200, headers = List("content-range" -> s"0-0/$count"), body = count.toString.getBytes)
      val datastore = new MockStackMobDatastore(getResponse,
        ResponseDetails(200),
        ResponseDetails(200),
        ResponseDetails(200))
      val (map, obj, _, _) = super.defaults
      (map, obj, datastore, dataService(datastore))
    }
    private val (_, _, datastore, svc) = defaults
    def correctSchema = {
      val countRes = svc.countObjects(schemaName) must beEqualTo(count)
      val correctSchema = datastore.getCalls.get(0).schema must beEqualTo(schemaName)
      countRes and correctSchema
    }
  }

}
