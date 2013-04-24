package com.stackmob.customcode.dev.server.sdk

import com.stackmob.sdkapi.http.{Header => CCHeader}
import com.stackmob.sdkapi.http.response.{HttpResponse => CCHttpResponse}
import com.stackmob.newman.{Headers => NewmanHeaders}
import com.stackmob.newman.response.{HttpResponse => NewmanHttpResponse}
import collection.JavaConverters._
import com.stackmob.newman.dsl._
import java.util.concurrent.Callable

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.sdk.http
 *
 * User: aaron
 * Date: 3/28/13
 * Time: 5:41 PM
 */
package object http {
  def newmanHeaders(headerSet: Set[CCHeader]): NewmanHeaders = {
    val ccHeaderList = headerSet.map { header =>
      header.getName -> header.getValue
    }
    NewmanHeaders(ccHeaderList.toList)
  }

  def ccHeaders(headers: NewmanHeaders): JSet[CCHeader] = {
    headers.map { headerList =>
      headerList.map { header =>
        new CCHeader(header._1, header._2)
      }.list.toSet.asJava
    }.getOrElse {
      new JHashSet[CCHeader]()
    }
  }

  case class CustomCCHttpResponse(code: Int, headers: JSet[CCHeader], body: String) extends CCHttpResponse(code, headers, body)

  def ccHttpResponse(resp: NewmanHttpResponse): CCHttpResponse = {
    val code = resp.code.code
    val headers = ccHeaders(resp.headers)
    val bodyString = resp.bodyString()
    CustomCCHttpResponse(code, headers, bodyString)
  }

  def ccHttpResponse(builder: Builder): CCHttpResponse = {
    ccHttpResponse(builder.prepare.unsafePerformIO)
  }

  def callable[T](fn: => T): Callable[T] = new Callable[T] {
    def call(): T = {
      fn
    }
  }
}
