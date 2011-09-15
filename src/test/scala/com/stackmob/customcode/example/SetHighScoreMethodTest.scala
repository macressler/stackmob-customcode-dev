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

package com.stackmob.customcode.example

import org.junit.Test
import org.junit.Assert._
import com.stackmob.customcode.localrunner._
import com.stackmob.core.MethodVerb
import collection.JavaConversions._

class SetHighScoreMethodTest {
  val method = new SetHighScoreMethod().getMethodName
  val entryPoint = new EntryPointExtender
  val runner = CustomCodeMethodRunnerFactory.getForScala(entryPoint, List("users"))

  @Test
  def newHighScore() {
    val res = runner.run(MethodVerb.GET, method, Map("username" -> "aaron", "score" -> "22"))
    val map = res.getResponseMap
    assertTrue(map.get("updated").asInstanceOf[Boolean])
    assertTrue(map.get("newUser").asInstanceOf[Boolean])
    assertTrue("aaron".equals(map.get("username").toString))
    assertTrue("22".equals(map.get("newScore").toString))
  }
}