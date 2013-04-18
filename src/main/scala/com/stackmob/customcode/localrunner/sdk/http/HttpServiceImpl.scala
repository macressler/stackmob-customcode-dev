package com.stackmob.customcode.localrunner.sdk.http

import com.stackmob.sdkapi.http.HttpService

import java.util.concurrent.{TimeUnit, Executors, Future}
import com.stackmob.sdkapi.http.request.{GetRequest, PostRequest, PutRequest, DeleteRequest}
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.newman.ApacheHttpClient
import com.stackmob.newman.dsl._
import collection.JavaConverters._
import com.stackmob.sdkapi.http.exceptions.{WhitelistException, RateLimitedException, TimeoutException, AccessDeniedException}
import com.twitter.util.Duration
import com.stackmob.customcode.localrunner.sdk.simulator.{ThrowableFrequency, Frequency, ErrorSimulator}

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.http
 * 
 * User: aaron
 * Date: 3/28/13
 * Time: 5:37 PM
 */
class HttpServiceImpl extends HttpService {
  private implicit val newmanClient = new ApacheHttpClient()

  private val rateLimitedFreq = ThrowableFrequency(new RateLimitedException, Frequency(1, Duration(1, TimeUnit.MINUTES)))
  private val whitelistedFreq = ThrowableFrequency(new WhitelistException("test domain"), Frequency(1, Duration(1, TimeUnit.MINUTES)))
  private val accessDeniedSimulator = ErrorSimulator(rateLimitedFreq :: whitelistedFreq :: Nil)
  private val timeoutFreq = ThrowableFrequency(new TimeoutException("test url"), Frequency(1, Duration(1, TimeUnit.MINUTES)))
  private val allSimulators = accessDeniedSimulator.and(timeoutFreq :: Nil)

  private lazy val executorService = Executors.newFixedThreadPool(4)

  override def isWhitelisted(url: String) = {
    true
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def get(req: GetRequest): HttpResponse = {
    allSimulators {
      ccHttpResponse {
        GET(req.getUrl)
          .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
      }
    }
  }

  @throws(classOf[AccessDeniedException])
  override def getAsync(req: GetRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      executorService.submit(callable(get(req)))
    }
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def post(req: PostRequest): HttpResponse = {
    allSimulators {
      ccHttpResponse {
        POST(req.getUrl)
          .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
          .addBody(req.getBody)
      }
    }
  }

  @throws(classOf[AccessDeniedException])
  override def postAsync(req: PostRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      executorService.submit(callable(post(req)))
    }
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def put(req: PutRequest): HttpResponse = {
    allSimulators {
      ccHttpResponse {
        PUT(req.getUrl)
          .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
          .addBody(req.getBody)
      }
    }
  }

  @throws(classOf[AccessDeniedException])
  override def putAsync(req: PutRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      executorService.submit(callable(put(req)))
    }
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def delete(req: DeleteRequest): HttpResponse = {
    allSimulators {
      ccHttpResponse {
        DELETE(req.getUrl)
          .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
      }
    }
  }

  @throws(classOf[AccessDeniedException])
  override def deleteAsync(req: DeleteRequest): Future[HttpResponse] = {
    accessDeniedSimulator {
      executorService.submit(callable(delete(req)))
    }
  }
}
