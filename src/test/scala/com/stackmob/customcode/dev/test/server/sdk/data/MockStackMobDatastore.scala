package com.stackmob.customcode.dev
package test
package server
package sdk
package data

import com.stackmob.sdk.api.{StackMobOptions, StackMobQuery, StackMobDatastore}
import com.stackmob.sdk.callback.StackMobRawCallback
import com.stackmob.sdk.net.{HttpVerbWithoutPayload, HttpVerb, HttpVerbWithPayload}
import com.stackmob.customcode.dev.server.sdk.{JavaMap, JavaList}
import collection.JavaConverters._
import java.util.concurrent.CopyOnWriteArrayList
import scalaz.Scalaz._
import com.stackmob.customcode.dev.server.sdk.EntryW

private[data] class MockStackMobDatastore(val getResponse: ResponseDetails, val postResponse: ResponseDetails)
  extends StackMobDatastore(datastoreExecutorService,datastoreSession, "MockStackMobDatastoreHost", datastoreRedirectedCallback) {
  import MockStackMobDatastore._

  lazy val getCalls = new CopyOnWriteArrayList[RequestDetails]()
  lazy val numGetCalls = getCalls.size()

  lazy val postCalls = new CopyOnWriteArrayList[RequestDetails]()
  lazy val numPostCalls = postCalls.size()

  private val requestURL = "http://testurl.com"
  private val emptyRequestHeaders = List[JavaMap.Entry[String, String]]().asJava
  private val getVerb = HttpVerbWithoutPayload.GET
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

  override def get(query: StackMobQuery, cb: StackMobRawCallback) {
    cb.setDone(getVerb, requestURL, emptyRequestHeaders, "", getResponse.code, getResponse.headerEntries, getResponse.body)
    getCalls.add(new RequestDetails(getVerb,
      query.getObjectName,
      query.getHeaders.asScala.toList,
      None,
      query.getArguments.asScala.map(_.tup).toList))
  }

  override def get(query: StackMobQuery, options: StackMobOptions, cb: StackMobRawCallback) {
    cb.setDone(getVerb, requestURL, emptyRequestHeaders, "", getResponse.code, getResponse.headerEntries, getResponse.body)
    getCalls.add(new RequestDetails(getVerb,
      query.getObjectName,
      query.getHeaders.asScala.toList ++ options.getHeaders.asScala.map(_.tup).toList,
      None,
      query.getArguments.asScala.map(_.tup).toList))
  }
}

private[data] class ResponseDetails(val code: Int,
                                    val headers: List[(String, String)] = Nil,
                                    val body: Array[Byte] = Array[Byte]()) {
  def headerEntries: JavaList[JavaMap.Entry[String, String]] = headers.toEntries
}

object MockStackMobDatastore {
  class RequestDetails(val verb: HttpVerb,
                       val schema: String,
                       val headers: List[(String, String)],
                       val body: Option[String],
                       val queryStringParams: List[(String, String)] = Nil)
}
