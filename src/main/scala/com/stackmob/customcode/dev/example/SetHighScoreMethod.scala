/**
 * Copyright 2011 StackMob
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

package com.stackmob.customcode.dev.example

import com.stackmob.core._
import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.SDKServiceProvider;
import collection.JavaConversions._

class SetHighScoreMethod extends CustomCodeMethod {

  private def getParam(request:ProcessedAPIRequest, name:String) = request.getParams.get(name) match {
    case null => None
    case "" => None
    case param => Some(param)
  }

  override def getMethodName = "set_high_score"
  override def getParams = seqAsJavaList(List("username", "score"))

  override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
    val username = getParam(request, "username") match {
      case None => return new ResponseToProcess(400, Map("error" -> "username was empty"))
      case Some(u) => u
    }
    val score = getParam(request, "score") match {
      case None => return new ResponseToProcess(400, Map("error" -> "score was empty"))
      case Some(s) => Integer.parseInt(s)
    }

    // get the datastore service and assemble the query
    val datastoreService = serviceProvider.getDatastoreService
    val query = Map("username" -> seqAsJavaList(List(username)))

    // execute the query
    try {
      val result = datastoreService.readObjects("users", query)

      val newUser = result == null || result.size() == 0

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

      new ResponseToProcess(200,
        Map("updated" -> updated, "newUser" -> newUser, "username" -> username, "newScore" -> userMap("score")))
    } catch {
      case e: InvalidSchemaException =>
        new ResponseToProcess(500, Map("error" -> "invalid schema", "detail" -> e.toString))
      case e: DatastoreException =>
        new ResponseToProcess(500, Map("error" -> "datastore exception", "detail" -> e.toString))
      case e: Throwable =>
        new ResponseToProcess(500, Map("error" -> "unknown", "detail" -> e.toString))
    }
  }
}