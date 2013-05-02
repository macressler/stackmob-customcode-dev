package com.stackmob.customcode.dev
package test
package server
package sdk
package data

import com.stackmob.sdk.api.{StackMob, StackMobDatastore}
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

  private val requestURL = "http://testurl.com"
  private val emptyRequestHeaders = List[JavaMap.Entry[String, String]]().asJava
  private val postVerb = HttpVerbWithPayload.POST

  override def post(schema: String,
                    body: String,
                    cb: StackMobRawCallback) {
    cb.setDone(postVerb,
      requestURL,
      emptyRequestHeaders,
      body,
      postResponse.code,
      postResponse.headerEntries,
      postResponse.body)

    postCalls.add(new RequestDetails(postVerb, schema, emptyRequestHeaders.toTuples, body.some))
  }

  override def postRelated(path: String,
                           primaryId: String,
                           relatedField: String,
                           relatedObject: Object,
                           cb: StackMobRawCallback) {
    cb.setDone(postVerb,
      requestURL,
      emptyRequestHeaders,
      relatedObject.toString,
      postResponse.code,
      postResponse.headerEntries,
      postResponse.body)
    postCalls.add(new RequestDetails(postVerb, s"$path/$primaryId/$relatedField", emptyRequestHeaders.toTuples, relatedObject.toString.some))
  }
}

object MockStackMobDatastore {
  class RequestDetails(val verb: HttpVerb,
                       val schema: String,
                       val headers: List[(String, String)],
                       val body: Option[String])
}
