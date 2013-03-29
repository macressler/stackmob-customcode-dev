package com.stackmob.customcode.localrunner.sdk.http

import com.stackmob.sdkapi.http.HttpService

import java.util.concurrent.{Executors, Future}
import com.stackmob.sdkapi.http.request.{GetRequest, PostRequest, PutRequest, DeleteRequest}
import com.stackmob.sdkapi.http.response.HttpResponse
import com.stackmob.newman.ApacheHttpClient
import com.stackmob.newman.dsl._
import collection.JavaConverters._

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

  private lazy val executorService = Executors.newFixedThreadPool(4)

  override def isWhitelisted(url: String) = {
    true
  }

  override def get(req: GetRequest): HttpResponse = {
    ccHttpResponse {
      GET(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
    }
  }

  override def getAsync(req: GetRequest): Future[HttpResponse] = {
    executorService.submit(callable(get(req)))
  }

  override def post(req: PostRequest): HttpResponse = {
    ccHttpResponse {
      POST(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
        .addBody(req.getBody)
    }
  }

  override def postAsync(req: PostRequest): Future[HttpResponse] = {
    executorService.submit(callable(post(req)))
  }

  override def put(req: PutRequest): HttpResponse = {
    ccHttpResponse {
      PUT(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
        .addBody(req.getBody)
    }
  }

  override def putAsync(req: PutRequest): Future[HttpResponse] = {
    executorService.submit(callable(put(req)))
  }

  override def delete(req: DeleteRequest): HttpResponse = {
    ccHttpResponse {
      DELETE(req.getUrl)
        .addHeaders(newmanHeaders(req.getHeaders.asScala.toSet))
    }
  }

  override def deleteAsync(req: DeleteRequest): Future[HttpResponse] = {
    executorService.submit(callable(delete(req)))
  }


}
