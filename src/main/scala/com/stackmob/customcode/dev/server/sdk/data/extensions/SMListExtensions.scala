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

package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi.{SMValue, SMList}
import net.liftweb.json._
import collection.JavaConverters._

trait SMListExtensions {
  implicit class SMListW(val smList: SMList[_ <: SMValue[_]]) {
    def toObject(depth: Int = 0): Object = {
      checkDepth(depth) {
        val javaList = smList.getValue
        val objects = javaList.asScala.map { rawT =>
          rawT.toObject(depth + 1)
        }
        objects.asJava
      }
    }

    def toJValue(depth: Int = 0): JValue = {
      checkDepth(depth) {
        val jValues = smList.getValue.asScala.map { rawT =>
          rawT.toJValue(depth + 1)
        }.toList
        JArray(jValues)
      }
    }
  }
}
