package com.stackmob.customcode.dev
package test
package server

import org.specs2.Specification
import com.stackmob.newman.test.DummyHttpClient
import com.stackmob.customcode.dev.server.APIRequestProxy
import com.stackmob.customcode.dev.server.APIRequestProxy.UnknownVerbError
import collection.JavaConverters._

class APIRequestProxySpecs extends Specification with CustomMatchers { def is =
  "APIRequestProxySpecs".title                                                                                          ^ end ^
  "APIRequestProxy is responsible for executing non-custom code requests to the Stackmob API"                           ^ end ^
  "the proxy should fail if given an unknown verb"                                                                      ! unknownVerb ^ end ^
  "the proxy should work properly for GET requests"                                                                     ! get ^ end ^
  "the proxy should work properly for POST requests"                                                                    ! post ^ end ^
  "the proxy should work properly for PUT requests"                                                                     ! put ^ end ^
  "the proxy should work properly for DELETE requests"                                                                  ! delete ^ end ^
  "the proxy should work properly for HEAD requests"                                                                    ! head ^ end ^
  end

  private val resp = DummyHttpClient.CannedResponse
  private implicit def client = new DummyHttpClient(responseToReturn = () => resp)

  private def request(verb: String,
                      uri: String,
                      headers: Map[String, String] = Map("Content-Type" -> "text/plain"),
                      body: String = "stackmob-test") = {
    new MockJettyRequest(verb, uri, headers.asJava, body)
  }

  private def unknownVerb = {
    val req = request("OPTIONS", "http://httpbin.org/options")
    APIRequestProxy(req).toEither must beThrowableInstance[UnknownVerbError]
  }

  private def get = {
    val req = request("GET", "http://httpbin.org/get")
    APIRequestProxy(req).toEither must beRight
  }

  private def post = {
    val req = request("POST", "http://httpbin.org/post")
    APIRequestProxy(req).toEither must beRight
  }

  private def put = {
    val req = request("PUT", "http://httpbin.org/put")
    APIRequestProxy(req).toEither must beRight
  }

  private def delete = {
    val req = request("DELETE", "http://httpbin.org/delete")
    APIRequestProxy(req).toEither must beRight
  }

  private def head = {
    val req = request("HEAD", "http://httpbin.org/head")
    APIRequestProxy(req).toEither must beRight
  }
}
