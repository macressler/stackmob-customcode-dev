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
package scalaexamples

import com.stackmob.core._
import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.ProcessedAPIRequest
import com.stackmob.core.rest.ResponseToProcess
import com.stackmob.sdkapi.SDKServiceProvider
import collection.JavaConversions._
import javax.servlet.http.HttpServletResponse
import com.stackmob.customcode.dev.server.sdk.JavaList

class SetHighScoreMethod extends CustomCodeMethod {

  private def getParam(request:ProcessedAPIRequest, name:String) = Option(request.getParams.get(name)).flatMap { param =>
    if (param.isEmpty) {
      None
    } else {
      Some(param)
    }
  }

  override def getMethodName: String = "set_high_score"
  override def getParams: JavaList[String] = seqAsJavaList(List("username", "score"))

  private class EmptyUsernameException extends Exception("username was empty") {
    val responseToProcess = {
      new ResponseToProcess(HttpServletResponse.SC_BAD_REQUEST, Map("error" -> "username was empty"))
    }
  }
  private class EmptyScoreException extends Exception("score was empty") {
    val responseToProcess = {
      new ResponseToProcess(HttpServletResponse.SC_BAD_REQUEST, Map("error" -> "score was empty"))
    }
  }

  override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
    lazy val username = getParam(request, "username") match {
      case None => throw new EmptyUsernameException//
      case Some(u) => u
    }
    lazy val score = getParam(request, "score") match {
      case None => throw new EmptyScoreException
      case Some(s) => Integer.parseInt(s)
    }

    // get the datastore service and assemble the query
    lazy val datastoreService = serviceProvider.getDatastoreService
    lazy val query = Map("username" -> seqAsJavaList(List(username)))

    // execute the query
    try {
      val result = datastoreService.readObjects("users", query)

      val newUser = (!Option(result).isDefined) || result.isEmpty

      var userMap:Map[String, Object] = newUser match {
        //not a new user so fetch from the datastore map
        case false => result.get(0).asInstanceOf[Map[String, Object]]
        //new user so create the map
        case true => Map("username" -> username, "score" -> new Integer(0))
      }

      val oldScore = userMap("score").toString.toInt

      val updated = oldScore < score
      if(updated) userMap = userMap - "score" + (("score", new Integer(score)))

      newUser match {
        //create new user in datastore
        case true => datastoreService.createObject("users", userMap)
        //update existing user in datastore
        case false => datastoreService.updateObject("users", username, userMap)
      }

      new ResponseToProcess(HttpServletResponse.SC_OK,
        Map("updated" -> updated, "newUser" -> newUser, "username" -> username, "newScore" -> userMap("score")))
    } catch {
      case e: EmptyScoreException => {
        e.responseToProcess
      }
      case e: EmptyUsernameException => {
        e.responseToProcess
      }
      case e: InvalidSchemaException => {
        new ResponseToProcess(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map("error" -> "invalid schema", "detail" -> e.toString))
      }
      case e: DatastoreException => {
        new ResponseToProcess(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map("error" -> "datastore exception", "detail" -> e.toString))
      }
      case e: Throwable => {
        new ResponseToProcess(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map("error" -> "unknown", "detail" -> e.toString))
      }
    }
  }
}
