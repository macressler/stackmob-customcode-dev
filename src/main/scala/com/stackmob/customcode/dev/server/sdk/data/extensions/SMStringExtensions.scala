package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi.SMString
import net.liftweb.json._

trait SMStringExtensions {
  implicit class SMStringW(val smString: SMString) {
    def toObject: Object = {
      smString.getValue
    }
    def toJValue: JValue = {
      JString(smString.getValue)
    }
  }
}
