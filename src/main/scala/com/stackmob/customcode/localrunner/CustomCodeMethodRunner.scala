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

import com.stackmob.core._
import com.stackmob.core.rest._
import com.stackmob.core.customcode._
import com.stackmob.core.jar._
import collection.JavaConversions.{asScalaBuffer, mapAsJavaMap, mapAsScalaMap}

abstract class CustomCodeMethodRunner(entryObject: JarEntryObject, initialModels:List[String]) {

  protected val methodsList = asScalaBuffer(entryObject.methods()).toList
  protected val methodsMap:Map[String, CustomCodeMethod] = methodsList.map((obj:CustomCodeMethod) => (obj.getMethodName, obj)).toMap
  val appName = "app_"+entryObject.getClass.getName
  val apiVersion = 0

  protected def runImpl(verb:MethodVerb, method:String, params:Map[String, String]):ResponseToProcess = {
    val sdkServiceProvider = new SDKServiceProviderMockImpl(entryObject.getClass.getName, initialModels)
    val url = "http://test/"+method
    val processedAPIRequest = new ProcessedAPIRequest(verb, url, null, params, appName, apiVersion, method, 0)

    val ccMethod = methodsMap(method)
    ccMethod.execute(processedAPIRequest, sdkServiceProvider)
  }
}

/**
 * CustomCodeMethodRunner suited for use with Java
 */
class CustomCodeMethodRunnerJavaAdapter(entryObject:JarEntryObject, initialModels:java.util.List[String])
  extends CustomCodeMethodRunner(entryObject, asScalaBuffer(initialModels).toList) {
  def run(verb:MethodVerb, method:String, params:java.util.Map[String, String]) =
     runImpl(verb, method, mapAsScalaMap(params).toMap)
}

/**
 * CustomCodeMethodRunner suited for use with Scala
 */
class CustomCodeMethodRunnerScalaAdapter(entryObject:JarEntryObject, initialModels:List[String])
  extends CustomCodeMethodRunner(entryObject, initialModels) {
  def run(verb:MethodVerb, method:String, params:Map[String, String]) = runImpl(verb, method, params)
}

/**
 * Factory for building CustomCodeMethodRunners
 */
object CustomCodeMethodRunnerFactory {
  def getForJava(entryObject:JarEntryObject, initialModels:java.util.List[String]) =
    new CustomCodeMethodRunnerJavaAdapter(entryObject, initialModels)

  def getForScala(entryObject:JarEntryObject, initialModels:List[String]) =
    new CustomCodeMethodRunnerScalaAdapter(entryObject, initialModels)
}