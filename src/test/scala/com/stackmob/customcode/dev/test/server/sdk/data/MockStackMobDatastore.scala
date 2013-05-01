package com.stackmob.customcode.dev
package test
package server
package sdk
package data

import com.stackmob.sdk.api.StackMobDatastore
import com.stackmob.sdk.callback.StackMobRawCallback
import com.stackmob.sdk.net.{HttpVerb, HttpVerbWithPayload}
import com.stackmob.customcode.dev.server.sdk.{JavaMap, JavaList}
import collection.JavaConverters._
import java.util.concurrent.CopyOnWriteArrayList
import scalaz.Scalaz._

private[data] class ResponseDetails(val code: Int,
                                    val headers: List[(String, String)] = Nil,
                                    val body: Array[Byte] = Array[Byte]()) {
  def headerEntries: JavaList[JavaMap.Entry[String, String]] = headers.toEntries
}

private[data] class MockStackMobDatastore(postResponse: ResponseDetails)
  extends StackMobDatastore(datastoreExecutorService,datastoreSession, "MockStackMobDatastoreHost", datastoreRedirectedCallback) {
  import MockStackMobDatastore._

  lazy val postCalls = new CopyOnWriteArrayList[RequestDetails]()
  lazy val numPostCalls = postCalls.size()

  override def post(schema: String, body: String, cb: StackMobRawCallback) {
    val requestVerb = HttpVerbWithPayload.POST
    val requestURL = "http://test-url.com"
    val requestHeaders = List[JavaMap.Entry[String, String]]().asJava
    val requestBody = body
    val responseStatusCode = postResponse.code
    val responseHeaders = postResponse.headerEntries
    val responseBody = postResponse.body
    cb.setDone(requestVerb, requestURL, requestHeaders, requestBody, responseStatusCode, responseHeaders, responseBody)
    postCalls.add(new RequestDetails(requestVerb, schema, requestHeaders.toTuples, requestBody.some))
  }
}

object MockStackMobDatastore {
  class RequestDetails(val verb: HttpVerb,
                       val schema: String,
                       val headers: List[(String, String)],
                       val body: Option[String])
}
