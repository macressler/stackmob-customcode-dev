package com.stackmob.customcode.dev
package localrunner

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

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 2:47 PM
 */
class CustomCodeHandler(jarEntry: JarEntryObject,
                        maxMethodDuration: Duration = 25.seconds)
                       (implicit executionContext: ExecutionContext = CustomCodeMethodExecutor.DefaultExecutionContext)
  extends AbstractHandler {

  private lazy val logger = LoggerFactory.getLogger(classOf[CustomCodeHandler])

  private val methods = jarEntry.methods.asScala.foldLeft(Map[String, CustomCodeMethod]()) { (running, method) =>
    running ++ Map(method.getMethodName -> method)
  }

  //TODO: get these from config file
  private lazy val apiKey = "cc-test-api-key"
  private lazy val apiSecret = "cc-test-api-secret"

  private lazy val stackMob = stackMobClient(apiKey, apiSecret)
  private lazy val stackMobPush = stackMobPushClient(apiKey, apiSecret)

  override def handle(target: String,
                      baseRequest: Request,
                      servletRequest: HttpServletRequest,
                      response: HttpServletResponse) {
    val writer = response.getWriter
    val realPath = baseRequest.getPathInfo.replaceFirst("/", "")

    methods.get(realPath).map { method =>
      val body = baseRequest.getReader.exhaust().toString()
      val sdkServiceProvider = new SDKServiceProviderImpl(stackMob, stackMobPush)

      val resTry = for {
        apiReq <- processedAPIRequest(realPath, baseRequest, servletRequest, body)
        resp <- CustomCodeMethodExecutor(method,
          apiReq,
          sdkServiceProvider,
          maxMethodDuration)(executionContext)
        respJSON <- Try {
          val respMap = resp.getResponseMap
          json.write(respMap.asScala)
        }
        _ <- Try(response.setStatus(resp.getResponseCode))
        _ <- Try(writer.print(respJSON))
      } yield ()

      try {
        resTry.get
      } catch {
        case t: TimeoutException => {
          //note - if the future is in an infinite loop, it will continue to take up the thread on which it's executing until the server is killed
          logger.warn(s"${method.getMethodName} took over ${maxMethodDuration.toSeconds} seconds to execute")
          writer.print(s"${method.getMethodName} took over ${maxMethodDuration.toSeconds} seconds) to execute. Please shorten its execution time")
        }
        case t: Throwable => {
          logger.warn(s"${method.getMethodName} threw ${t.getMessage}", t)
          writer.print(s"${method.getMethodName} threw ${t.getMessage}. see logs for details")
        }
      }
    }.getOrElse {
      logger.debug(s"unknown custom code method $realPath. attempting to proxy the request to v0 of your API")
      //TODO: proxy to API
      writer.println("unknown custom code method %s".format(realPath))
      response.setStatus(404)
    }

    baseRequest.setHandled(true)
  }

}
