package com.stackmob.customcode.dev
package test
package server

import org.specs2.Specification
import org.specs2.mock.Mockito
import com.stackmob.newman.test.DummyHttpClient
import org.eclipse.jetty.server.Request
import scala.util.Try
import com.stackmob.customcode.dev.server.APIRequestProxy
import com.stackmob.customcode.dev.server.APIRequestProxy.UnknownVerbError

class APIRequestProxySpecs extends Specification with Mockito with CustomMatchers { def is =
  "APIRequestProxySpecs".title                                                                                          ^ end ^
  "APIRequestProxy is responsible for executing non-custom code requests to the Stackmob API"                           ^ end ^
  "the proxy should fail if given an unknown verb"                                                                      ! unknownVerb ^ end ^
  end

  private val resp = DummyHttpClient.CannedResponse
  private implicit def client = new DummyHttpClient(responseToReturn = () => resp)

  private def unknownVerb = {
    val req = new Request()
    req.setMethod("OPTIONS")
    APIRequestProxy(req).toEither must beThrowableInstance[UnknownVerbError]
  }
}
