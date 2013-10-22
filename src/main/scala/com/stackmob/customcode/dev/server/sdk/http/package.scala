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

package com.stackmob.customcode.dev.server.sdk

import com.stackmob.sdkapi.http.{Header => CCHeader}
import com.stackmob.sdkapi.http.response.{HttpResponse => CCHttpResponse}
import com.stackmob.newman.{Headers => NewmanHeaders}
import com.stackmob.newman.response.{HttpResponse => NewmanHttpResponse}
import collection.JavaConverters._
import com.stackmob.newman.dsl._
import java.util.concurrent.{TimeUnit, Future, Callable}
import scala.util.Try
import scala.concurrent.Await
import scala.concurrent.duration._

package object http {
  def newmanHeaders(headerSet: Set[CCHeader]): NewmanHeaders = {
    val ccHeaderList = headerSet.map { header =>
      header.getName -> header.getValue
    }
    NewmanHeaders(ccHeaderList.toList)
  }

  def ccHeaders(headers: NewmanHeaders): JavaSet[CCHeader] = {
    headers.map { headerList =>
      headerList.map { header =>
        new CCHeader(header._1, header._2)
      }.list.toSet.asJava
    }.getOrElse {
      new JavaHashSet[CCHeader]()
    }
  }

  case class CustomCCHttpResponse(code: Int, headers: JavaSet[CCHeader], body: String) extends CCHttpResponse(code, headers, body)

  def ccHttpResponse(resp: NewmanHttpResponse): CCHttpResponse = {
    val code = resp.code.code
    val headers = ccHeaders(resp.headers)
    val bodyString = resp.bodyString()
    CustomCCHttpResponse(code, headers, bodyString)
  }

  def ccHttpResponse(builder: Builder): CCHttpResponse = {
    ccHttpResponse(Await.result(builder.apply, maxCustomCodeMethodDuration))
  }

  def callable[T](fn: => T): Callable[T] = new Callable[T] {
    override def call(): T = {
      fn
    }
  }

  implicit class JavaFutureW[T](f: Future[T]) {
    def getSoon: Try[T] = {
      Try(f.get(1, TimeUnit.SECONDS))
    }
  }

  lazy val maxCustomCodeMethodDuration: Duration = 25.seconds
}
