package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi._
import com.stackmob.customcode.dev.server._
import net.liftweb.json.Implicits._
import net.liftweb.json._

trait SMPrimitiveExtensions {
  implicit class SMPrimitiveW[T](val smPrimitive: SMPrimitive[T]) {
    def toObject(depth: Int = 0): Object = checkDepth[Object](depth) {
      smPrimitive match {
        case i: SMInt => java.lang.Long.valueOf((i: SMInt).getValue)
        case l: SMLong => java.lang.Long.valueOf((l: SMLong).getValue)
        case d: SMDouble => java.lang.Double.valueOf((d: SMDouble).getValue)
        case b: SMBoolean => java.lang.Boolean.valueOf((b: SMBoolean).getValue)
      }
    }
    def toJValue(depth: Int = 0) = checkDepth(depth) {
      smPrimitive match {
        case smInt: SMInt => long2jvalue((smInt: SMInt).getValue)
        case smLong: SMLong => long2jvalue((smLong: SMLong).getValue)
        case smDouble: SMDouble => JDouble((smDouble: SMDouble).getValue)
        case smBool: SMBoolean => JBool((smBool: SMBoolean).getValue)
      }
    }
  }
}
