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

package com.stackmob.customcode.localrunner

import com.stackmob.customcode.example.EntryPointExtender
import com.sun.net.httpserver.HttpExchange
import org.mockito.Mockito._
import java.net.URI
import java.io.OutputStream
import com.stackmob.core.MethodVerb
import com.google.gson.Gson
import org.specs2.Specification
import org.specs2.mock.Mockito

class CustomCodeMethodServerHandlerSpecs extends Specification with Mockito { def is =
  "CustomCodeMethodServer".title                                                                                        ^ end ^
                                                                                                                        end

  private lazy val methodName = "testmethod"
  private lazy val url = "http://localhost/api/0/testapp/" + methodName + "?username=aaron&score=2345"
  private lazy val entryObject = new EntryPointExtender
  private lazy val initialModels = List("users")
  private lazy val method = entryObject.methods.get(0)
  private lazy val expectedResponseMap = Map("updated" -> true, "newUser" -> true, "username" -> "aaron", "newScore" -> "2345")
  private lazy val expectedResponseBytes = new Gson().toJson(expectedResponseMap).getBytes

  private lazy val mockOutputStream = mock[OutputStream]

  private lazy val runner = CustomCodeMethodRunnerFactory.getForScala(entryObject, initialModels)
  private lazy val handler = new CustomCodeMethodServerHandler(runner, method, MethodVerb.GET)

  private def constructExchange(uri:URI,
                                verb:MethodVerb = MethodVerb.GET,
                                outputStream:OutputStream = mockOutputStream): HttpExchange = {
    val ex = mock[HttpExchange]
    ex.getRequestMethod returns verb.toString
    ex.getRequestURI returns uri
    ex.getResponseBody returns outputStream
    ex
  }

  implicit private def stringToURI(s:String) = new URI(s)

  def normalHandle() = {
    val mockExchange = constructExchange(url)
    handler.handle(mockExchange)
    verify(mockExchange).sendResponseHeaders(200, 0)
    verify(mockExchange.getResponseBody).write(expectedResponseBytes)
  }

  def wrongParams() = {
    val invalidURL = "http://localhost/api/0/testapp/" + methodName
    val invalidExchange = constructExchange(invalidURL)

    handler.handle(invalidExchange)
    verify(invalidExchange).sendResponseHeaders(500, 0)
  }

  def invalidMethod() = {
    val invalidURL = "http://localhost/api/0/testapp/invalid_method"
    val invalidExchange = constructExchange(invalidURL)

    handler.handle(invalidExchange)
    verify(invalidExchange).sendResponseHeaders(500, 0)
  }
}