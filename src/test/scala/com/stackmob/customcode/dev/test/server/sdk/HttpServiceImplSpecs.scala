package com.stackmob.customcode.dev
package test
package server
package sdk

import org.specs2.{ScalaCheck, Specification}
import com.stackmob.customcode.dev.server.sdk.http.HttpServiceImpl
import com.stackmob.sdkapi.http.request. _
import com.stackmob.customcode.dev.server.sdk.simulator.{Frequency, ThrowableFrequency}
import com.twitter.util.Duration
import java.util.concurrent.{Executors, Future, TimeUnit}
import scala.util.Try
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.sdkapi.http.Header
import collection.JavaConverters._

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
  private def impl = {
    new HttpServiceImpl(rateLimitedFreq = throwableFreq0,
      whitelistedFreq = throwableFreq0,
      timeoutFreq = throwableFreq0,
      executorService = Executors.newFixedThreadPool(16))
  }

  private val baseUrl = "http://httpbin.org"
  private val headers = Set(new Header("X-StackMob-Test", "StackMobTestHeader"))
  private val body = "StackMobTestBody"

  private val getRequest = new GetRequest(s"$baseUrl/get", headers.asJava)
  private val postRequest = new PostRequest(s"$baseUrl/post", headers.asJava, body)
  private val putRequest = new PutRequest(s"$baseUrl/put", headers.asJava, body)
  private val deleteRequest = new DeleteRequest(s"$baseUrl/delete", headers.asJava)

  private def resolveFuture(fn: => Future[HttpResponse])
                           (implicit timeMagnitude: Int = 1,
                            timeUnit: TimeUnit = TimeUnit.SECONDS): Either[Throwable, HttpResponse] = {
    Try {
      fn.get(timeMagnitude, timeUnit)
    }.toEither
  }

  private lazy val emptyArgs: String = {
    """"args": {}"""
  }

  private def dataJson(data: String): String = {
    //the scala compiler doesn't seem to like 2.10 style format strings here
    """"data": "%s"""".format(data)
  }

  private def get = {
    val res = impl.get(getRequest)
    res must beResponse(200, """"args": {}""")
  }

  private def getAsync = {
    resolveFuture(impl.getAsync(getRequest)) must beRight.like {
      case r => r must beResponse(200, emptyArgs)
    }
  }

  private def post = {
    impl.post(postRequest) must beResponse(200, dataJson(postRequest.getBody))
  }

  private def postAsync = {
    resolveFuture(impl.postAsync(postRequest)) must beRight.like {
      case r => r must beResponse(200, dataJson(postRequest.getBody))
    }
  }

  private def put = {
    impl.put(putRequest) must beResponse(200, dataJson(putRequest.getBody))
  }

  private def putAsync = {
    resolveFuture(impl.putAsync(putRequest)) must beRight.like {
      case r => r must beResponse(200, dataJson(putRequest.getBody))
    }
  }

  private def delete = {
    impl.delete(deleteRequest) must beResponse(200, emptyArgs)
  }

  private def deleteAsync = {
    resolveFuture(impl.deleteAsync(deleteRequest)) must beRight.like {
      case r => r must beResponse(200, emptyArgs)
    }
  }
}
