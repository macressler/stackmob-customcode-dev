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

import org.eclipse.jetty.server.Request
import com.stackmob.newman.response.HttpResponse
import com.stackmob.newman.{HttpClient, ApacheHttpClient}
import scala.util.Try
import com.stackmob.newman.request.HttpRequestType
import com.stackmob.newman.dsl._

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
