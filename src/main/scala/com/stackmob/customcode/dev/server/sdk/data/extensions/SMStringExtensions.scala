package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi.SMString
import com.stackmob.customcode.dev.server._
import net.liftweb.json._

trait SMStringExtensions {
  implicit class SMStringExtensions(val smString: SMString) {
    def toObject(depth: Int = 0): Object = checkDepth(maxDepth) {
      smString.getValue
    }
    def toJValue(depth: Int = 0): JValue = checkDepth(maxDepth) {
      JString(smString.getValue)
    }
  }
}
