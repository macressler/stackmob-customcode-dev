package com.stackmob.customcode.dev

import scalaz.{Validation, Success, Failure}
import scalaz.Scalaz._
import java.util.UUID
import java.io.BufferedReader
import org.eclipse.jetty.server.Request
import javax.servlet.http.HttpServletRequest
import com.stackmob.core.rest.ProcessedAPIRequest
import com.stackmob.core.MethodVerb
import collection.JavaConverters._
import scala.util.Try
import org.eclipse.jetty.http.HttpURI

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

  lazy val uuid = UUID.randomUUID()

  private def fmt(s: String) = "cc-localrunner-%s-%s".format(s, uuid)

  lazy val appName = fmt("app")
  lazy val userSchemaName = fmt("user-schema")
  lazy val userName = fmt("user")
  lazy val loggedInUser = fmt("logged-in-user")
  lazy val testFBUser = fmt("facebook-user")
  lazy val testFBMessageID = fmt("facebook-message")
  lazy val testTwitterUser = fmt("twitter-user")

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

  def getQueryParams(httpURI: HttpURI): Map[String, String] = {
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
                          body: String): Try[ProcessedAPIRequest] = {
    for {
      requestedVerb <- Try(MethodVerb.valueOf(servletReq.getMethod))
      httpURI <- Try(baseReq.getUri)
      queryParams <-Try(getQueryParams(httpURI))
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
