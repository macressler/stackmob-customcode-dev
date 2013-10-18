/**
 * Copyright 2011-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.dev
package server
package sdk
package http

import com.stackmob.sdkapi.http.HttpService
import java.util.concurrent.{TimeUnit, Future}
import com.stackmob.sdkapi.http.request._
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.newman.dsl._
import collection.JavaConverters._
import com.stackmob.sdkapi.http.exceptions.{WhitelistException, RateLimitedException, TimeoutException, AccessDeniedException}
import com.twitter.util.Duration
import simulator.{ThrowableFrequency, Frequency, ErrorSimulator}
import HttpServiceImpl._
import com.google.common.util.concurrent.SettableFuture
import com.stackmob.newman.{ApacheHttpClient, HttpClient}

class HttpServiceImpl(rateLimitedFreq: ThrowableFrequency = DefaultRateLimitedThrowableFrequency,
                      whitelistedFreq: ThrowableFrequency = DefaultWhitelistedThrowableFrequency,
                      timeoutFreq: ThrowableFrequency = DefaultTimeoutThrowableFrequency,
                      newmanClient: HttpClient = DefaultNewmanClient) extends HttpService {

  private val accessDeniedSimulator = ErrorSimulator(rateLimitedFreq :: whitelistedFreq :: Nil)
  private val allSimulators = accessDeniedSimulator.and(timeoutFreq :: Nil)

  private implicit val client = newmanClient
  private implicit lazy val ec = ApacheHttpClient.newmanRequestExecutionContext

  private def async(bld: => Builder): Future[HttpResponse] = {
    val settableFuture = SettableFuture.create[HttpResponse]()
    bld.apply.map { res =>
      settableFuture.set(ccHttpResponse(res))
    }
    settableFuture
  }

  override def isWhitelisted(url: String): Boolean = {
    true
  }

  //GET

  private def doGetAsync(req: GetRequest): Future[HttpResponse] = async {
    GET(req.getUrl)
      .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def get(req: GetRequest): HttpResponse = {
    allSimulators {
      doGetAsync(req).getSoon.mapFailure {
        case t: JavaTimeoutException => new TimeoutException(req.getUrl.toString)
      }
    }.get
  }

  @throws(classOf[AccessDeniedException])
  override def getAsync(req: GetRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      doGetAsync(req)
    }
  }


  //POST

  private def doPostAsync(req: PostRequest): Future[HttpResponse] = async {
    POST(req.getUrl)
      .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
      .addBody(req.getBody)
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def post(req: PostRequest): HttpResponse = {
    allSimulators {
      doPostAsync(req).getSoon.mapFailure {
        case t: JavaTimeoutException => new TimeoutException(req.getUrl.toString)
      }.get
    }
  }

  @throws(classOf[AccessDeniedException])
  override def postAsync(req: PostRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      doPostAsync(req)
    }
  }

  //PUT

  private def doPutAsync(req: PutRequest): Future[HttpResponse] = async {
    PUT(req.getUrl)
      .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
      .addBody(req.getBody)
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def put(req: PutRequest): HttpResponse = {
    allSimulators {
      doPutAsync(req).getSoon.mapFailure {
        case t: JavaTimeoutException => new TimeoutException(req.getUrl.toString)
      }.get
    }
  }

  @throws(classOf[AccessDeniedException])
  override def putAsync(req: PutRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      doPutAsync(req)
    }
  }


  //DELETE

  private def doDeleteAsync(req: DeleteRequest): Future[HttpResponse] = async {
    DELETE(req.getUrl)
      .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def delete(req: DeleteRequest): HttpResponse = {
    allSimulators {
      doDeleteAsync(req).getSoon.mapFailure {
        case t: JavaTimeoutException => new TimeoutException(req.getUrl.toString)
      }.get
    }
  }

  @throws(classOf[AccessDeniedException])
  override def deleteAsync(req: DeleteRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      doDeleteAsync(req)
    }
  }
}

object HttpServiceImpl {
  lazy val DefaultRateLimitedThrowableFrequency = ThrowableFrequency(new RateLimitedException, Frequency(2, Duration(1, TimeUnit.HOURS)))
  lazy val DefaultWhitelistedThrowableFrequency = ThrowableFrequency(new WhitelistException("test domain"), Frequency(2, Duration(1, TimeUnit.HOURS)))
  lazy val DefaultTimeoutThrowableFrequency = ThrowableFrequency(new TimeoutException("test url"), Frequency(1, Duration(2, TimeUnit.HOURS)))
  //keep this lazy so a thread pool isn't created possibly needlessly
  lazy val DefaultNewmanClient = new ApacheHttpClient()
}
