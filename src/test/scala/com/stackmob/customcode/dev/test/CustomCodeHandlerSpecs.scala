package com.stackmob.customcode.dev.test

import org.specs2.Specification
import com.stackmob.customcode.dev.server.CustomCodeHandler
import org.specs2.mock.Mockito
import org.mockito.Matchers
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.PrintWriter
import collection.JavaConverters._
import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi.SDKServiceProvider
import com.stackmob.core.jar.JarEntryObject
import java.util.UUID
import com.stackmob.newman.test.DummyHttpClient
import com.stackmob.customcode.dev.test.server.MockJettyRequest
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CustomCodeHandlerSpecs extends Specification with Mockito { def is =
  "CustomCodeHandlerSpecs".title                                                                                        ^ end ^
  "CustomCodeHandler is the entry point for all custom code HTTP requests"                                              ^ end ^
  "a standard CC method call should work"                                                                               ! standardCCMethod ^ end ^
  "a non-CC method call should work"                                                                                    ! nonCCMethod ^ end ^
  end

  private lazy val methodName = "helloworld"
  private lazy val methodParams = List[String]().asJava
  private lazy val methodReturnCode = 200
  private lazy val methodReturnStr = "helloWorldResp"
  private lazy val method: CustomCodeMethod = new CustomCodeMethod {
    override lazy val getMethodName = methodName
    override lazy val getParams = methodParams
    override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
      new ResponseToProcess(methodReturnCode, Map("msg" -> methodReturnStr).asJava)
    }
  }
  private lazy val jarEntryObject = new JarEntryObject {
    override lazy val methods = List(method).asJava
  }

  private lazy val apiKey = "api_key"
  private lazy val apiSecret = "api_secret"

  private implicit def sessionUUID = UUID.randomUUID()
  private implicit val dummyHttpClient = new DummyHttpClient()
  private def handler = new CustomCodeHandler(apiKey, apiSecret, jarEntryObject)

  private def jettyArgs(verbString: String,
                        methodName: String): (MockJettyRequest, HttpServletRequest, PrintWriter, HttpServletResponse) = {
    val responseWriter = {
      val w = mock[PrintWriter]
      w
    }
    val request = {
      new MockJettyRequest(verbString, s"http://test.com/$methodName", Map[String, String]().asJava, "")
    }

    val servletRequest = {
      val r = mock[HttpServletRequest]
      r.getMethod returns verbString
      r
    }

    val servletResponse = {
      val r = mock[HttpServletResponse]
      r.getWriter returns responseWriter
      r
    }
    (request, servletRequest, responseWriter, servletResponse)
  }

  private def standardCCMethod = {

    val (request, servletRequest, responseWriter, servletResponse) = jettyArgs("GET", methodName)

    handler.handle(methodName, request, servletRequest, servletResponse)

    val cType = there was one(servletResponse).setContentType(Matchers.eq("application/json"))
    val status = there was one(servletResponse).setStatus(Matchers.eq(200))
    val handled = request.getHandled must beEqualTo(true)
    val written = there was one(responseWriter).println(Matchers.contains(methodReturnStr))

    cType and status and handled and written
  }

  private def nonCCMethod = {
    val returnedResponse = Await.result(dummyHttpClient.responseToReturn, Duration.Inf)
    val methodName = UUID.randomUUID().toString
    val (request, servletRequest, responseWriter, servletResponse) = jettyArgs("GET", methodName)
    handler.handle(methodName, request, servletRequest, servletResponse)

    val status = there was one(servletResponse).setStatus(returnedResponse.code.code)
    val handled = request.getHandled must beEqualTo(true)
    val written = there was one(responseWriter).println(Matchers.eq(returnedResponse.bodyString))

    status and handled and written
  }

}
