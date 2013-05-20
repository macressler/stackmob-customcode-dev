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

import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi.SDKServiceProvider
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors
import scala.util.Try

object CustomCodeMethodExecutor {
  val DefaultMaxMethodDuration = 25.seconds
  val DefaultExecutionContext = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  def apply(method: CustomCodeMethod,
            req: ProcessedAPIRequest,
            provider: SDKServiceProvider,
            maxMethodDuration: Duration = 25.seconds)
           (implicit exContext: ExecutionContext = DefaultExecutionContext): Try[ResponseToProcess] = {
    val respFuture = future(method.execute(req, provider))
    Try {
      Await.result(respFuture, maxMethodDuration)
    }
  }
}
