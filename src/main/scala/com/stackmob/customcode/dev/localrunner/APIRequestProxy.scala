package com.stackmob.customcode.dev.localrunner

import org.eclipse.jetty.server.Request
import com.stackmob.newman.response.HttpResponse
import com.stackmob.newman.{HttpClient, ApacheHttpClient}
import scala.util.Try
import com.stackmob.newman.request.HttpRequestType
import com.stackmob.newman.dsl._

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.localrunner
 *
 * User: aaron
 * Date: 4/18/13
 * Time: 5:14 PM
 */
object APIRequestProxy {

  val DefaultHttpClient = new ApacheHttpClient()
  class UnknownVerbError(verb: String) extends Exception(s"unknown HTTP verb $verb")

  def apply(req: Request)
           (implicit httpClient: HttpClient = DefaultHttpClient): Try[HttpResponse] = {
    for {
      newmanVerb <- req.getNewmanVerb match {
        case Some(v) => Try(v)
        case None => Try {
          throw new UnknownVerbError(req.getMethod.toUpperCase)
        }
      }
      url <- req.getURL
      headers <- Try(req.getAllHeaders)
      body <- Try(req.getBody)
      req <- newmanVerb match {
        case HttpRequestType.GET => {
          Try(GET(url).addHeaders(headers))
        }
        case HttpRequestType.POST => {
          Try(POST(url).addHeaders(headers).addBody(body))
        }
        case HttpRequestType.PUT => {
          Try(PUT(url).addHeaders(headers).addBody(body))
        }
        case HttpRequestType.DELETE => {
          Try(DELETE(url).addHeaders(headers))
        }
        case HttpRequestType.HEAD => {
          Try(HEAD(url).addHeaders(headers))
        }
      }
    } yield {
      req.toRequest.executeUnsafe
    }
  }
}
