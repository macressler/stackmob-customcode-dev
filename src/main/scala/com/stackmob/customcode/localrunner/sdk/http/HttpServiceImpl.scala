package com.stackmob.customcode.localrunner.sdk.http

import com.stackmob.sdkapi.http.HttpService

import java.util.concurrent.{Executors, Future}
import com.stackmob.sdkapi.http.request.{GetRequest, PostRequest, PutRequest, DeleteRequest}
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.newman.ApacheHttpClient
import com.stackmob.newman.dsl._
import collection.JavaConverters._
import com.stackmob.sdkapi.http.exceptions.{TimeoutException, AccessDeniedException}

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
  //TODO: simulate rate limits, blacklists and whitelists
  private implicit val newmanClient = new ApacheHttpClient()

  private lazy val executorService = Executors.newFixedThreadPool(4)

  override def isWhitelisted(url: String) = {
    true
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def get(req: GetRequest): HttpResponse = {
    ccHttpResponse {
      GET(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
    }
  }

  @throws(classOf[AccessDeniedException])
  override def getAsync(req: GetRequest): Future[HttpResponse] = {
    executorService.submit(callable(get(req)))
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def post(req: PostRequest): HttpResponse = {
    ccHttpResponse {
      POST(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
        .addBody(req.getBody)
    }
  }

  @throws(classOf[AccessDeniedException])
  override def postAsync(req: PostRequest): Future[HttpResponse] = {
    executorService.submit(callable(post(req)))
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def put(req: PutRequest): HttpResponse = {
    ccHttpResponse {
      PUT(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
        .addBody(req.getBody)
    }
  }

  @throws(classOf[AccessDeniedException])
  override def putAsync(req: PutRequest): Future[HttpResponse] = {
    executorService.submit(callable(put(req)))
  }

  @throws(classOf[AccessDeniedException])
  @throws(classOf[TimeoutException])
  override def delete(req: DeleteRequest): HttpResponse = {
    ccHttpResponse {
      DELETE(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
    }
  }

  @throws(classOf[AccessDeniedException])
  override def deleteAsync(req: DeleteRequest): Future[HttpResponse] = {
    executorService.submit(callable(delete(req)))
  }


}
