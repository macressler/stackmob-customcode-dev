package com.stackmob.customcode.dev
package tester

import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.customcode.dev.server.sdk.SDKServiceProviderImpl
import java.util.concurrent.{TimeoutException => JavaTimeoutException}
import concurrent.{TimeoutException => ScalaTimeoutException}
import java.util.UUID

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.tester
 *
 * User: aaron
 * Date: 4/18/13
 * Time: 4:31 PM
 */
object LocalTester {
  /**
   * execute a custom code method locally
   * @param method the method to execute
   * @param req the request to pass to the custom code method
   * @param apiKey the api key to use to proxy requests for the push and datastore services
   * @param apiSecret the api secret to use to proxy requests for the push and datastore services
   * @return the result of executing {{method}}
   */
  @throws(classOf[JavaTimeoutException])
  def executeMethod(method: CustomCodeMethod,
                    req: ProcessedAPIRequest,
                    apiKey: String,
                    apiSecret: String): ResponseToProcess = {
    implicit val session = UUID.randomUUID()
    val stackMob = stackMobClient(apiKey, apiSecret)
    val stackMobPush = stackMobPushClient(apiKey, apiSecret)
    val provider = new SDKServiceProviderImpl(stackMob, stackMobPush)
    try {
      CustomCodeMethodExecutor(method, req, provider).get
    } catch {
      case st: ScalaTimeoutException => {
        throw new JavaTimeoutException(s"""
          ${method.getMethodName} took over ${CustomCodeMethodExecutor.DefaultMaxMethodDuration.toSeconds} seconds to execute
        """)
      }
    }
  }
}
