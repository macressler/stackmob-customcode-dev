package com.stackmob.customcode.dev
package server
package sdk
package http

import com.stackmob.sdkapi.http.HttpService
import java.util.concurrent.{ExecutorService, TimeUnit, Executors, Future}
import com.stackmob.sdkapi.http.request._
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.newman.ApacheHttpClient
import com.stackmob.newman.dsl._
import collection.JavaConverters._
import com.stackmob.sdkapi.http.exceptions.{WhitelistException, RateLimitedException, TimeoutException, AccessDeniedException}
import com.twitter.util.Duration
import simulator.{ThrowableFrequency, Frequency, ErrorSimulator}
import HttpServiceImpl._
import scalaz.concurrent.Strategy
import com.google.common.util.concurrent.SettableFuture

class HttpServiceImpl(rateLimitedFreq: ThrowableFrequency = DefaultRateLimitedThrowableFrequency,
                      whitelistedFreq: ThrowableFrequency = DefaultWhitelistedThrowableFrequency,
                      timeoutFreq: ThrowableFrequency = DefaultTimeoutThrowableFrequency,
                      executorService: ExecutorService = DefaultExecutorService) extends HttpService {

  private implicit val newmanClient = new ApacheHttpClient(strategy = Strategy.Executor(executorService))

  private val accessDeniedSimulator = ErrorSimulator(rateLimitedFreq :: whitelistedFreq :: Nil)
  private val allSimulators = accessDeniedSimulator.and(timeoutFreq :: Nil)

  private def async(bld: => Builder): Future[HttpResponse] = {
    val settableFuture = SettableFuture.create[HttpResponse]()
    bld.prepareAsync.unsafePerformIO.map { res =>
      settableFuture.set(ccHttpResponse(res))
    }
    settableFuture
  }

  override def isWhitelisted(url: String) = {
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
  lazy val DefaultExecutorService = Executors.newCachedThreadPool
}
