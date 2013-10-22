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
import com.stackmob.sdkapi._
import javax.servlet.http.HttpServletResponse
import com.stackmob.customcode.dev.server.sdk.JavaList
import scala.collection.JavaConverters._

class SetHighScoreMethod extends CustomCodeMethod {

  private def getParam(request:ProcessedAPIRequest, name:String) = Option(request.getParams.get(name)).flatMap { param =>
    if (param.isEmpty) {
      None
    } else {
      Some(param)
    }
  }

  override def getMethodName: String = "set_high_score"
  override def getParams: JavaList[String] = List("username", "score").asJava

  private class EmptyUsernameException extends Exception("username was empty") {
    val responseToProcess = {
      new ResponseToProcess(HttpServletResponse.SC_BAD_REQUEST, Map("error" -> "username was empty").asJava)
    }
  }
  private class EmptyScoreException extends Exception("score was empty") {
    val responseToProcess = {
      new ResponseToProcess(HttpServletResponse.SC_BAD_REQUEST, Map("error" -> "score was empty").asJava)
    }
  }

  private def responseHelper(updated: Boolean,
                             newUser: Boolean,
                             username: String,
                             newScore: Int): ResponseToProcess = {
    new ResponseToProcess(HttpServletResponse.SC_OK,
      Map("updated" -> updated, "newUser" -> newUser, "username" -> username, "newScore" -> newScore).asJava)
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
    lazy val dataService = serviceProvider.getDataService
    lazy val query = List[SMCondition](new SMEquals("username", new SMString(username))).asJava

    // execute the query
    try {
      val result = dataService.readObjects("users", query).asScala
      val response = result.headOption match {

        // existing user pathway, which sends an SMUpdate
        case Some(userObj) => {
          val userMap = userObj.getValue.asScala
          val oldScore = userMap("score").toString.toInt

          if(oldScore < score) {
            val update = List[SMUpdate](new SMSet("score", new SMInt(score))).asJava
            dataService.updateObject("users", username, update)
            responseHelper(true, false, username, score)
          } else {
            responseHelper(false, false, username, score)
          }
        }

        // new user pathway, which creates a new SMObject
        case None => {
          val user = new SMObject(
            Map[String, SMValue[_]]("username" -> new SMString(username), "score" -> new SMInt(score)).asJava)
          dataService.createObject("users", user)
          responseHelper(true, true, username, score)
        }
      }
      response
    } catch {
      case e: EmptyScoreException => {
        e.responseToProcess
      }
      case e: EmptyUsernameException => {
        e.responseToProcess
      }
      case e: InvalidSchemaException => {
        new ResponseToProcess(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          Map("error" -> "invalid schema", "detail" -> e.toString).asJava)
      }
      case e: DatastoreException => {
        new ResponseToProcess(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          Map("error" -> "datastore exception", "detail" -> e.toString).asJava)
      }
      case e: Throwable => {
        new ResponseToProcess(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          Map("error" -> "unknown", "detail" -> e.toString).asJava)
      }
    }
  }
}
