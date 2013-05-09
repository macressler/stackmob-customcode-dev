package com.stackmob.customcode.dev
package test
package server
package sdk

import com.stackmob.customcode.dev.server.sdk.JavaList
import org.specs2.{ScalaCheck, Specification}
import com.stackmob.customcode.dev.server.sdk.http.HttpServiceImpl
import com.stackmob.sdkapi.http.request. _
import com.stackmob.customcode.dev.server.sdk.simulator.{Frequency, ThrowableFrequency}
import com.twitter.util.Duration
import java.util.concurrent.{Future, TimeUnit}
import scala.util.Try
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.sdkapi.http.{HttpService, Header}
import collection.JavaConverters._
import com.stackmob.newman.test.DummyHttpClient
import com.stackmob.newman.response.{HttpResponse => NewmanHttpResponse}
import org.specs2.matcher.MatchResult

class HttpServiceImplSpecs extends Specification with CustomMatchers with ScalaCheck { def is =
  "HttpServiceImplSpecs".title                                                                                          ^ end ^
  "HttpService is the custom code interface to make HTTP requests"                                                      ^ end ^
  "get should return immediately"                                                                                       ! get ^ end ^
  "getAsync should return within a reasonable time"                                                                     ! getAsync ^ end ^
  "post should return the POST body immediately"                                                                        ! post ^ end ^
  "postAsync should return the POST body within a reasonable time"                                                      ! postAsync ^ end ^
  "put should return the PUT body immediately"                                                                          ! put ^ end ^
  "putAsync should return the PUT body within a reasonable time"                                                        ! putAsync ^ end ^
  "delete should return immediately"                                                                                    ! delete ^ end ^
  "deleteAsync should return within a reasonable time"                                                                  ! deleteAsync ^ end ^
  end

  val throwableFreq0 = ThrowableFrequency(new Exception(""), Frequency(0, Duration(0, TimeUnit.SECONDS)))

  private val baseUrl = "http://httpbin.org"
  private val headers = Set(new Header("X-StackMob-Test", "StackMobTestHeader"))
  private val body = "StackMobTestBody"

  private val getRequest = new GetRequest(s"$baseUrl/get", headers.asJava)
  private val postRequest = new PostRequest(s"$baseUrl/post", headers.asJava, body)
  private val putRequest = new PutRequest(s"$baseUrl/put", headers.asJava, body)
  private val deleteRequest = new DeleteRequest(s"$baseUrl/delete", headers.asJava)

  private def resolveFuture(fn: => Future[HttpResponse])
                           (implicit timeMagnitude: Int = 1000,
                            timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Either[Throwable, HttpResponse] = {
    Try {
      fn.get(timeMagnitude, timeUnit)
    }.toEither
  }

  private def run[T](fn: (DummyHttpClient, HttpService) => T): T = {
    val dummyClient = new DummyHttpClient()
    val impl = new HttpServiceImpl(rateLimitedFreq = throwableFreq0,
      whitelistedFreq = throwableFreq0,
      timeoutFreq = throwableFreq0,
      newmanClient = dummyClient)
    fn(dummyClient, impl)
  }

  private def responseAndRequest[CallListType](givenResp: HttpResponse,
                                               expectedResp: NewmanHttpResponse,
                                               callList: JavaList[CallListType],
                                               expectedNumCalls: Int = 1): MatchResult[Any] = {
    val resp = givenResp must beResponse(expectedResp)
    val numCalls = callList.size must beEqualTo(expectedNumCalls)
    resp and numCalls
  }

  private def get = run { (dummyClient, impl) =>
    responseAndRequest(impl.get(getRequest), dummyClient.responseToReturn(), dummyClient.getRequests)
  }

  private def getAsync = run { (dummyClient, impl) =>
    resolveFuture(impl.getAsync(getRequest)) must beRight.like {
      case r => responseAndRequest(r, dummyClient.responseToReturn(), dummyClient.getRequests)
    }
  }

  private def post = run { (dummyClient, impl) =>
    responseAndRequest(impl.post(postRequest), dummyClient.responseToReturn(), dummyClient.postRequests)
  }

  private def postAsync = run { (dummyClient, impl) =>
    resolveFuture(impl.postAsync(postRequest)) must beRight.like {
      case r => responseAndRequest(r, dummyClient.responseToReturn(), dummyClient.postRequests)
    }
  }

  private def put = run { (dummyClient, impl) =>
    responseAndRequest(impl.put(putRequest), dummyClient.responseToReturn(), dummyClient.putRequests)
  }

  private def putAsync = run { (dummyClient, impl) =>
    resolveFuture(impl.putAsync(putRequest)) must beRight.like {
      case r => responseAndRequest(r, dummyClient.responseToReturn(), dummyClient.putRequests)
    }
  }

  private def delete = run { (dummyClient, impl) =>
    responseAndRequest(impl.delete(deleteRequest), dummyClient.responseToReturn(), dummyClient.deleteRequests)
  }

  private def deleteAsync = run { (dummyClient, impl) =>
    resolveFuture(impl.deleteAsync(deleteRequest)) must beRight.like {
      case r => responseAndRequest(r, dummyClient.responseToReturn(), dummyClient.deleteRequests)
    }
  }
}
