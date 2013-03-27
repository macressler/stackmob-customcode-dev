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

package com.stackmob.customcode.localrunner.functional

import com.stackmob.customcode.localrunner.CustomCodeMethodServer
import com.stackmob.customcode.example.EntryPointExtender
import org.specs2._


//class CustomCodeMethodServerTests extends Specification {
//
//  val entryObject = new EntryPointExtender
//  val initialModels = List("users")
//  val appName = "testapp"
//  val server = new CustomCodeMethodServer(entryObject, initialModels, appName)
//  val runningServerThread = new Thread {
//    override def run() {
//      server.serve()
//    }
//  }
//
//  runningServerThread.setDaemon(true)
//  runningServerThread.start()
//
//  val validURL = "http://localhost/api/0/"+appName+"/set_high_score?username=aaron&score=2345"
//  "a valid running server" should {
//    responseMap must be matching("hello world")
//  }
//}