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

package com.stackmob.customcode.dev.server

import net.liftweb.json.{NoTypeHints, Serialization}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.json
 *
 * User: aaron
 * Date: 3/28/13
 * Time: 4:40 PM
 */
package object json {
  private implicit val formats = Serialization.formats(NoTypeHints)

  def read[T: Manifest](s: String): T = {
    Serialization.read[T](s)
  }
  def write[T <: AnyRef](t: T): String = {
    Serialization.write[T](t)
  }

}
