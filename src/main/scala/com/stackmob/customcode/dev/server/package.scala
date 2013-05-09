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
 * com.stackmob.customcode.server
 *
 * User: aaron
 * Date: 3/27/13
 * Time: 4:53 PM
 */
package object server {
  lazy val maxDepth = 2
  class SMValueDepthLimitReached(depth: Int) extends Exception(s"the maximum SMValue depth of $depth has been reached")
  object SMValueDepthLimitReached {
    def apply(depth: Int) = {
      new SMValueDepthLimitReached(depth)
    }
  }

  class SMConditionDepthLimitReached(depth: Int) extends Exception(s"the maximum SMCondition depth of $depth has been reached")
  object SMConditionDepthLimitReached {
    def apply(depth: Int) = {
      new SMConditionDepthLimitReached(depth)
    }
  }


  private def fmt(s: String)(implicit session: UUID) = "cc-server-%s-%s".format(s, session)

  def appName(implicit session: UUID) = fmt("app")
  def userSchemaName(implicit session: UUID) = fmt("user-schema")
  def userName(implicit session: UUID) = fmt("user")
  def loggedInUser(implicit session: UUID) = fmt("logged-in-user")
  def testFBUser(implicit session: UUID) = fmt("facebook-user")
  def testFBMessageID(implicit session: UUID) = fmt("facebook-message")
  def testTwitterUser(implicit session: UUID) = fmt("twitter-user")

  def validating[T](t: => T): Validation[Throwable, T] = {
    try {
      t.success[Throwable]
    } catch {
      case t: Throwable => t.fail[T]
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
      val allHeaderNamesEnumeration = req.getHeaderNames
      val allHeaderNames = allHeaderNamesEnumeration.asScala.map(_.asInstanceOf[String])
      allHeaderNames.foldLeft(List[(String, String)]()) { (agg, cur) =>
        agg ++ List(cur -> req.getHeader(cur))
      }
    }

    def getURL: Try[URL] = {
      val strBuf = req.getRequestURL
      Try(new URL(strBuf.toString))
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
