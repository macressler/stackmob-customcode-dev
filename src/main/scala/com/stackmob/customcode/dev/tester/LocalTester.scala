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
package tester

import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.customcode.dev.server.sdk.SDKServiceProviderImpl
import java.util.concurrent.{TimeoutException => JavaTimeoutException}
import concurrent.{TimeoutException => ScalaTimeoutException}
import java.util.UUID

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
                    apiSecret: String,
                    config: ConfigMap = DefaultConfig): ResponseToProcess = {
    implicit val session = UUID.randomUUID()
    val stackMob = stackMobClient(apiKey, apiSecret)
    val stackMobPush = stackMobPushClient(apiKey, apiSecret)
    val provider = new SDKServiceProviderImpl(stackMob, stackMobPush, config)
    try {
      CustomCodeMethodExecutor(method, req, provider).get
    } catch {
      case st: ScalaTimeoutException => {
        val s = s"${method.getMethodName} took over ${CustomCodeMethodExecutor.DefaultMaxMethodDuration.toSeconds} seconds to execute"
        throw new JavaTimeoutException(s)
      }
    }
  }
}
