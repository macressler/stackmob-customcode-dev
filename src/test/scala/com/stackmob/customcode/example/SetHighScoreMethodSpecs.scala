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

import com.stackmob.customcode.localrunner._
import com.stackmob.core.MethodVerb
import collection.JavaConverters._
import org.specs2.Specification

class SetHighScoreMethodSpecs extends Specification { def is =
  "SetHighScoreMethod".title                                                                                            ^ end ^
  """
  SetHighScoreMethod is a test method that uses the datastore to set a high score
  """                                                                                                                   ^ end ^
  "the method correctly updates"                                                                                        ! methodWorks ^ end ^
                                                                                                                        end

  private lazy val method = new SetHighScoreMethod().getMethodName
  private lazy val entryPoint = new EntryPointExtender
  private lazy val runner = CustomCodeMethodRunnerFactory.getForScala(entryPoint, List("users"))

  def methodWorks = {
    val res = runner.run(MethodVerb.GET, method, Map("username" -> "aaron", "score" -> "22"))
    val map = res.getResponseMap.asScala
    val updatedRes = map.get("update") must beSome(true)
    val newUserRes = map.get("newUser") must beSome(true)
    val usernameRes = map.get("username") must beSome("aaron")
    val scoreRes = map.get("newScore") must beSome("22")

    updatedRes and newUserRes and usernameRes and scoreRes
  }

}