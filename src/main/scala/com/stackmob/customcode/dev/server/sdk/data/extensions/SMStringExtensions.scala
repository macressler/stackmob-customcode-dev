package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi.SMString
import net.liftweb.json._

trait SMStringExtensions {
  implicit class SMStringW(val smString: SMString) {
    def toObject(depth: Int = 0): Object = checkDepth(depth) {
      smString.getValue
    }
    def toJValue(depth: Int = 0): JValue = checkDepth(depth) {
      JString(smString.getValue)
    }
  }
}
