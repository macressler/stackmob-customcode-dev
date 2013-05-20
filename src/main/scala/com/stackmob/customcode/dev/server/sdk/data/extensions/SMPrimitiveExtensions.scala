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

import com.stackmob.sdkapi._
import net.liftweb.json.Implicits._
import net.liftweb.json._

trait SMPrimitiveExtensions {
  implicit class SMPrimitiveW[T](val smPrimitive: SMPrimitive[T]) {
    def toObject: Object = {
      smPrimitive match {
        case i: SMInt => java.lang.Long.valueOf((i: SMInt).getValue)
        case l: SMLong => java.lang.Long.valueOf((l: SMLong).getValue)
        case d: SMDouble => java.lang.Double.valueOf((d: SMDouble).getValue)
        case b: SMBoolean => java.lang.Boolean.valueOf((b: SMBoolean).getValue)
      }
    }

    def toJValue: JValue = {
      smPrimitive match {
        case smInt: SMInt => long2jvalue((smInt: SMInt).getValue)
        case smLong: SMLong => long2jvalue((smLong: SMLong).getValue)
        case smDouble: SMDouble => JDouble((smDouble: SMDouble).getValue)
        case smBool: SMBoolean => JBool((smBool: SMBoolean).getValue)
      }
    }
  }
}
