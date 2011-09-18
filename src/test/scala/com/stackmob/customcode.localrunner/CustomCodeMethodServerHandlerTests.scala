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
import org.junit.Test
import com.sun.net.httpserver.HttpExchange
import org.mockito.Mockito._
import java.net.URI
import java.io.OutputStream
import com.stackmob.core.MethodVerb
import com.google.gson.Gson

class CustomCodeMethodServerHandlerTests {

  val methodName = "testmethod"
  val url = "http://localhost/api/0/testapp/" + methodName + "?username=aaron&score=2345"
  val entryObject = new EntryPointExtender
  val initialModels = List("users")
  val method = entryObject.methods.get(0)
  val expectedResponseMap = Map("updated" -> true, "newUser" -> true, "username" -> "aaron", "newScore" -> "2345")
  val expectedResponseBytes = new Gson().toJson(expectedResponseMap).getBytes

  val mockOutputStream = mock(classOf[OutputStream])

  val runner = CustomCodeMethodRunnerFactory.getForScala(entryObject, initialModels)
  val handler = new CustomCodeMethodServerHandler(runner, method, MethodVerb.GET)

  private def constructExchange(uri:URI, verb:MethodVerb = MethodVerb.GET, outputStream:OutputStream = mockOutputStream) = {
    val ex = mock(classOf[HttpExchange])
    when(ex.getRequestMethod).thenReturn(verb.toString)
    when(ex.getRequestURI).thenReturn(uri)
    when(ex.getResponseBody).thenReturn(outputStream)
    ex
  }

  implicit private def stringToURI(s:String) = new URI(s)

  @Test
  def normalHandle() {
    val mockExchange = constructExchange(url)
    handler.handle(mockExchange)
    verify(mockExchange).sendResponseHeaders(200, 0)
    verify(mockExchange.getResponseBody).write(expectedResponseBytes)
  }

  @Test
  def wrongParams() {
    val invalidURL = "http://localhost/api/0/testapp/" + methodName
    val invalidExchange = constructExchange(invalidURL)

    handler.handle(invalidExchange)
    verify(invalidExchange).sendResponseHeaders(500, 0)
  }

  @Test
  def invalidMethod() {
    val invalidURL = "http://localhost/api/0/testapp/invalid_method"
    val invalidExchange = constructExchange(invalidURL)

    handler.handle(invalidExchange)
    verify(invalidExchange).sendResponseHeaders(500, 0)
  }
}