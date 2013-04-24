package com.stackmob.customcode.dev

import scalaz.{Validation, Success, Failure}
import scalaz.Scalaz._
import java.util.UUID
import java.io.BufferedReader
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.stackmob.core.rest.ProcessedAPIRequest
import com.stackmob.core.MethodVerb
import collection.JavaConverters._
import scala.util.Try
import org.eclipse.jetty.http.HttpURI
import com.stackmob.newman.request.HttpRequestType
import com.stackmob.newman.Headers
import java.net.URL

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner
 *
 * User: aaron
 * Date: 3/27/13
 * Time: 4:53 PM
 */
package object localrunner {
  lazy val maxDepth = 2
  class DepthLimitReached(depth: Int) extends Exception("the maximum depth of %d has been reached".format(depth))
  object DepthLimitReached {
    def apply(depth: Int) = {
      new DepthLimitReached(depth)
    }
  }

  def validating[T](t: => T): Validation[Throwable, T] = {
    try {
      t.success[Throwable]
    } catch {
      case t: Throwable => t.fail[T]
    }
  }

  implicit class ValidationW[Fail, Success](validation: Validation[Fail, Success]) {

    def mapFailure[NewFail](fn: Fail => NewFail): Validation[NewFail, Success] = {
      validation match {
        case Success(s) => Success(s)
        case Failure(t) => Failure(fn(t))
      }
    }
  }

  implicit class ThrowableValidationW[Success](validation: Validation[Throwable, Success]) {
    def getOrThrow: Success = {
      validation ||| { t: Throwable =>
        throw t
      }
    }
  }

  implicit class BufferedReaderW(val reader: BufferedReader) {
    def exhaust(builder: StringBuilder = new StringBuilder): StringBuilder = {
      Option(reader.readLine()).map { line =>
        exhaust(builder.append(line))
      }.getOrElse(builder)
    }
  }

  implicit class HttpURIW(val httpURI: HttpURI) {
    def getQueryParams: Map[String, String] = {
      val mbQueryString = Option(httpURI.getQuery)
      mbQueryString.map { queryString =>
        queryString.split("&").toList.foldLeft(Map[String, String]()) { (agg, cur) =>
          cur.split("=").toList match {
            case key :: value :: Nil => agg ++ Map(key -> value)
            case _ => agg
          }
        }
      }.getOrElse(Map[String, String]())
    }
  }

  implicit class HttpServletRequestW(val servletReq: HttpServletRequest) {
    def getMethodVerb: Try[MethodVerb] = {
      Try(MethodVerb.valueOf(servletReq.getMethod))
    }

    def getNewmanVerb: Option[HttpRequestType] = {
      val stringRep = servletReq.getMethod.toUpperCase
      stringRep match {
        case HttpRequestType.GET.stringVal => Some(HttpRequestType.GET)
        case HttpRequestType.POST.stringVal => Some(HttpRequestType.POST)
        case HttpRequestType.PUT.stringVal => Some(HttpRequestType.PUT)
        case HttpRequestType.DELETE.stringVal => Some(HttpRequestType.DELETE)
        case HttpRequestType.HEAD.stringVal => Some(HttpRequestType.HEAD)
        case _ => None
      }
    }
  }

  implicit class HttpServletResponseW(val servletRes: HttpServletResponse) {
    def setHeaders(headers: Headers) {
      val headerList = headers.map { headersNel =>
        headersNel.list
      }.getOrElse(List[(String, String)]())
      headerList.foreach { tup =>
        val (name, value) = tup
        servletRes.setHeader(name, value)
      }
    }
  }

  implicit class RequestW(val req: Request) {
    def getAllHeaders: List[(String, String)] = {
      val allHeaderNames = req.getHeaderNames.asScala.map(_.asInstanceOf[String])
      allHeaderNames.foldLeft(List[(String, String)]()) { (agg, cur) =>
        agg ++ List(cur -> req.getHeader(cur))
      }
    }

    def getURL: Try[URL] = {
      Try(new URL(req.getRequestURL.toString))
    }

    def getBody: String = {
      Option(req.getReader).map { reader =>
        reader.exhaust().toString()
      }.getOrElse("")
    }
  }

  /**
   * create a ProcessedAPIRequest
   * @param methodName the name of the method to execute
   * @param baseReq the Request
   * @param servletReq the servlet request
   * @param body the entire body of the request
   * @return the new ProcessedAPIRequest
   */
  def processedAPIRequest(methodName: String,
                          baseReq: Request,
                          servletReq: HttpServletRequest,
                          body: String)
                         (implicit session: UUID): Try[ProcessedAPIRequest] = {
    for {
      requestedVerb <- servletReq.getMethodVerb
      httpURI <- Try(baseReq.getUri)
      queryParams <-Try(httpURI.getQueryParams)
      apiVersion <- Try(0)
      counter <- Try(0)
    } yield {
      new ProcessedAPIRequest(requestedVerb,
        httpURI.toString,
        loggedInUser,
        queryParams.asJava,
        body,
        appName,
        apiVersion,
        methodName,
        counter)
    }
  }
}
