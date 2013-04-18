package com.stackmob.customcode.localrunner

import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi.SDKServiceProvider
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors
import scala.util.Try

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner
 *
 * User: aaron
 * Date: 4/18/13
 * Time: 2:50 PM
 */
object CustomCodeMethodExecutor {
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
