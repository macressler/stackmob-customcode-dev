package com.stackmob.customcode.dev
package test
package server

import org.specs2.Specification
import com.stackmob.newman.test.DummyHttpClient
import com.stackmob.customcode.dev.server.APIRequestProxy
import com.stackmob.customcode.dev.server.APIRequestProxy.UnknownVerbError
import collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.stackmob.newman.response.HttpResponse
import com.stackmob.newman.ApacheHttpClient

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

  private implicit def client = new DummyHttpClient(DummyHttpClient.CannedResponseFuture)
  private implicit lazy val ec = ApacheHttpClient.newmanRequestExecutionContext

  private def request(verb: String,
                      uri: String,
                      headers: Map[String, String] = Map("Content-Type" -> "text/plain"),
                      body: String = "stackmob-test") = {
    new MockJettyRequest(verb, uri, headers.asJava, body)
  }

  private def unknownVerb = {
    val req = request("OPTIONS", "http://httpbin.org/options")
    Await.result(APIRequestProxy(req), Duration.Inf) must throwA[UnknownVerbError]
  }

  private def get = {
    val req = request("GET", "http://httpbin.org/get")
    Await.result(APIRequestProxy(req), Duration.Inf) must beAnInstanceOf[HttpResponse]
  }

  private def post = {
    val req = request("POST", "http://httpbin.org/post")
    Await.result(APIRequestProxy(req), Duration.Inf) must beAnInstanceOf[HttpResponse]
  }

  private def put = {
    val req = request("PUT", "http://httpbin.org/put")
    Await.result(APIRequestProxy(req), Duration.Inf) must beAnInstanceOf[HttpResponse]
  }

  private def delete = {
    val req = request("DELETE", "http://httpbin.org/delete")
    Await.result(APIRequestProxy(req), Duration.Inf) must beAnInstanceOf[HttpResponse]
  }

  private def head = {
    val req = request("HEAD", "http://httpbin.org/head")
    Await.result(APIRequestProxy(req), Duration.Inf) must beAnInstanceOf[HttpResponse]
  }
}
