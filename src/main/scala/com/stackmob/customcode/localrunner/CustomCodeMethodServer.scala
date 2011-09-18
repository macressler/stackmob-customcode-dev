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

import java.net.InetSocketAddress
import java.util.concurrent.Executors
import com.sun.net.httpserver.{HttpServer, HttpExchange, HttpHandler}
import com.stackmob.core.jar.JarEntryObject
import com.stackmob.core.MethodVerb
import com.stackmob.core.customcode.CustomCodeMethod
import com.google.gson.Gson
import collection.JavaConversions._
import JavaConversions._

/**
 * a simple HTTP server that exposes your custom code methods via a REST interface for testing.
 */
class CustomCodeMethodServer(entryObject:JarEntryObject, initialModels:List[String], appName:String, port:Int = 8080) {

  val stopDelaySeconds = 2
  val methods = entryObject.methods()

  val runner = CustomCodeMethodRunnerFactory.getForScala(entryObject, initialModels)

  val server = HttpServer.create(new InetSocketAddress(port), 0)
  server.setExecutor(Executors.newCachedThreadPool());

  val contexts = for(m <- asScalaBuffer(methods).toList)
    yield server.createContext(getUrl(m.getMethodName), new CustomCodeMethodServerHandler(runner, m, MethodVerb.GET))

  protected def getUrl(methodName:String) = "/api/0/"+appName+"/"+methodName

  def serve() {
    server.start()
    println("StackMob Custom Code Method Development Server is listening on port " + port + " for app " + appName)
    println("with URLs: ")
    for(c <- contexts) println(c.getPath)
  }

  def shutdown() {
    println("shutting down server for app " + appName)
    server.stop(stopDelaySeconds)
    println("server shutdown")
  }

}

class CustomCodeMethodServerHandler(runner:CustomCodeMethodRunnerScalaAdapter, method:CustomCodeMethod, verb:MethodVerb)
  extends HttpHandler {

  def handle(exchange:HttpExchange) {
    try {
      val (code, map) = handleImpl(exchange)
      exchange.sendResponseHeaders(code, 0)
      val respString = new Gson().toJson(map)
      exchange.getResponseBody.write(respString.getBytes)
    }
    catch {
      case e:Throwable => {
        exchange.sendResponseHeaders(500, 0)
        val errString = "{\"error\":\"" + e.getMessage + "\"}"
        exchange.getResponseBody.write(errString.getBytes)
      }
    }
  }

  protected def handleImpl(exchange:HttpExchange) = {

    val requestedVerb = MethodVerb.valueOf(exchange.getRequestMethod)
    if(requestedVerb != verb)
      throw new Exception("method " + method.getMethodName + " is only available by " + verb.toString)

    val expectedParams:List[String] = method.getParams

    val queryStringOption = exchange.getRequestURI.getQuery match {
      case q:String => Some(q)
      case _ => None
    }
    val queryMap:Map[String, String] = queryStringOption match {
      case Some(queryString) => {
        val queryList = queryString.split("&").toList
        queryList.map(e => {
          val split = e.split("=")
          (split(0), split(1))
        }).toMap
      }
      case None => Map[String, String]()
    }

    if(expectedParams != queryMap.keySet.toList)
      throw new Exception("expected parameters " + expectedParams + " do not match actual params " + queryMap.keySet)

    val resp = runner.run(verb, method.getMethodName, queryMap)

    (resp.getResponseCode, mapAsScalaMap(resp.getResponseMap).toMap)

  }
}