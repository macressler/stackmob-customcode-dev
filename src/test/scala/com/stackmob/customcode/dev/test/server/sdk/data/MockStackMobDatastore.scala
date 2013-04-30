package com.stackmob.customcode.dev.test.server.sdk.data

import com.stackmob.sdk.api.StackMobDatastore
import java.util.concurrent.atomic.AtomicInteger
import com.stackmob.sdk.callback.StackMobRawCallback
import com.stackmob.sdk.net.HttpVerbWithPayload
import com.stackmob.customcode.dev.server.sdk.JavaMap
import collection.JavaConverters._

private[data] class MockStackMobDatastore(postResponseBody: Array[Byte])
  extends StackMobDatastore(datastoreExecutorService,datastoreSession, "MockStackMobDatastoreHost", datastoreRedirectedCallback) {
  lazy val numPostCalls = new AtomicInteger(0)

  override def post(schema: String, body: String, cb: StackMobRawCallback) {
    val requestVerb = HttpVerbWithPayload.POST
    val requestURL = "http://test-url.com"
    val requestHeaders = List[JavaMap.Entry[String, String]]().asJava
    val requestBody = "test-post-request-body"
    val responseStatusCode = 200
    val responseHeaders = List[JavaMap.Entry[String, String]]().asJava
    val responseBody = postResponseBody
    cb.setDone(requestVerb, requestURL, requestHeaders, requestBody, responseStatusCode, responseHeaders, responseBody)
    numPostCalls.incrementAndGet()
  }
}
