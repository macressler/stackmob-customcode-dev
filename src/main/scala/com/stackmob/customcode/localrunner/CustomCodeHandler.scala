package com.stackmob.customcode.localrunner

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.stackmob.core.jar.JarEntryObject
import collection.JavaConverters._
import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.ProcessedAPIRequest
import com.stackmob.core.MethodVerb
import java.io.BufferedReader
import sdk.SDKServiceProviderImpl
import net.liftweb.json.{NoTypeHints, Serialization}
import com.stackmob.sdk.api.StackMob
import com.stackmob.sdk.api.StackMob.OAuthVersion
import json._

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 2:47 PM
 */
class CustomCodeHandler(jarEntry: JarEntryObject) extends AbstractHandler {
  private val methods = jarEntry.methods.asScala.foldLeft(Map[String, CustomCodeMethod]()) { (running, method) =>
    running ++ Map(method.getMethodName -> method)
  }

  private def exhaustBufferedReader(reader: BufferedReader, builder: StringBuilder = new StringBuilder): StringBuilder = {
    Option(reader.readLine()).map { line =>
      exhaustBufferedReader(reader, builder.append(line))
    }.getOrElse(builder)
  }

  /**
   * create a ProcessedAPIRequest
   * @param methodName the name of the method to execute
   * @param baseReq the Request
   * @param servletReq the servlet request
   * @param body the entire body of the request
   * @return the new ProcessedAPIRequest
   */
  private def processedAPIRequest(methodName: String, baseReq: Request, servletReq: HttpServletRequest, body: String): ProcessedAPIRequest = {
    val requestedVerb = MethodVerb.valueOf(servletReq.getMethod)
    val httpURI = baseReq.getUri
    val mbQueryString = Option(httpURI.getQuery)
    val mbQueryParams = for {
      queryString <- mbQueryString
    } yield {
      queryString.split("&").toList.foldLeft(Map[String, String]()) { (agg, cur) =>
        cur.split("=").toList match {
          case key :: value :: Nil => agg ++ Map(key -> value)
          case _ => agg
        }
      }
    }
    val queryParams = mbQueryParams.getOrElse(Map[String, String]())

    val apiVersion = 0
    val counter = 0

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

  //TODO: get these from config file
  private lazy val apiKey = "cc-test-api-key"
  private lazy val apiSecret = "cc-test-api-secret"

  private lazy val stackmob = new StackMob(OAuthVersion.One, 0, apiKey, apiSecret)
  override def handle(target: String,
                      baseRequest: Request,
                      servletRequest: HttpServletRequest,
                      response: HttpServletResponse) {
    val writer = response.getWriter
    val realPath = baseRequest.getPathInfo.replaceFirst("/", "")

    methods.get(realPath).map { method =>
      val body = exhaustBufferedReader(baseRequest.getReader).toString()
      val apiReq = processedAPIRequest(realPath, baseRequest, servletRequest, body)
      val sdkServiceProvider = new SDKServiceProviderImpl(stackmob)
      val resp = method.execute(apiReq, sdkServiceProvider)
      val respMap = resp.getResponseMap
      val respJSON = json.write(respMap.asScala)

      response.setStatus(resp.getResponseCode)
      writer.print(respJSON)

    }.getOrElse {
      writer.println("unknown custom code method %s".format(realPath))
      response.setStatus(404)
    }

    baseRequest.setHandled(true)
  }

}
