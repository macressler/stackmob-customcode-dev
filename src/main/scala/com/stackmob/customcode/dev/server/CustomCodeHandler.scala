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

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.stackmob.core.jar.JarEntryObject
import collection.JavaConverters._
import com.stackmob.core.customcode.CustomCodeMethod
import sdk.SDKServiceProviderImpl
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import scala.util.Try
import com.stackmob.customcode.dev.CustomCodeMethodExecutor
import java.util.UUID
import com.stackmob.newman.HttpClient

class CustomCodeHandler(apiKey: String,
                        apiSecret: String,
                        jarEntry: JarEntryObject,
                        maxMethodDuration: Duration = 25.seconds,
                        config: ConfigMap = DefaultConfig)
                       (implicit executionContext: ExecutionContext = CustomCodeMethodExecutor.DefaultExecutionContext,
                        session: UUID,
                        newmanClient: HttpClient)
  extends AbstractHandler {

  private lazy val logger = LoggerFactory.getLogger(classOf[CustomCodeHandler])

  private val methods = jarEntry.methods.asScala.foldLeft(Map[String, CustomCodeMethod]()) { (running, method) =>
    running ++ Map(method.getMethodName -> method)
  }

  private lazy val stackMob = stackMobClient(apiKey, apiSecret)
  private lazy val stackMobPush = stackMobPushClient(apiKey, apiSecret)

  override def handle(target: String,
                      baseRequest: Request,
                      servletRequest: HttpServletRequest,
                      response: HttpServletResponse) {
    def writer = response.getWriter
    val realPath = baseRequest.getPathInfo.replaceFirst("/", "")

    val body = baseRequest.getBody

    methods.get(realPath).map { method =>

      val processedAPIRequestTry = processedAPIRequest(realPath, baseRequest, servletRequest, body)
      val sdkServiceProvider = new SDKServiceProviderImpl(stackMob, stackMobPush, config)

      val resTry = for {
        apiReq <- processedAPIRequestTry
        resp <- CustomCodeMethodExecutor(method,
          apiReq,
          sdkServiceProvider,
          maxMethodDuration)(executionContext)
        respJSON <- Try {
          val respMap = resp.getResponseMap
          json.write(respMap.asScala)
        }
        _ <- Try {
          response.setContentType("application/json")
        }
        _ <- Try {
          response.setStatus(resp.getResponseCode)
        }
        _ <- Try {
          baseRequest.setHandled(true)
        }
        _ <- Try {
          writer.println(respJSON)
        }
      } yield ()

      try {
        resTry.get
      } catch {
        case t: TimeoutException => {
          //note - if the future is in an infinite loop, it will continue to take up the thread on which it's executing until the server is killed
          logger.warn(s"${method.getMethodName} took over ${maxMethodDuration.toSeconds} seconds to execute")
          baseRequest.setHandled(true)
          writer.println(s"${method.getMethodName} took over ${maxMethodDuration.toSeconds} seconds) to execute. Please shorten its execution time")
        }
        case t: Throwable => {
          logger.warn(s"${method.getMethodName} threw ${t.getMessage}", t)
          baseRequest.setHandled(true)
          writer.println(s"${method.getMethodName} threw ${t.getMessage}. see logs for details")
        }
      }
    }.getOrElse {
      logger.debug(s"unknown custom code method $realPath. attempting to proxy the request to v0 of your API")
      val respTry = for {
        newmanResp <- Try(Await.result(APIRequestProxy(baseRequest), maxMethodDuration))
        _ <- Try(response.setStatus(newmanResp.code.code))
        _ <- Try(response.setHeaders(newmanResp.headers))
        _ <- Try(baseRequest.setHandled(true))
        _ <- Try(writer.println(newmanResp.bodyString))
      } yield {
        ()
      }

      try {
        respTry.get
      }
      catch {
        case t: Throwable => {
          logger.warn(s"proxy failed with ${t.getMessage}", t)
          baseRequest.setHandled(true)
          writer.print(s"proxy failed with ${t.getMessage}")
        }
      }
    }
  }
}
