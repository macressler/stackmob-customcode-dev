package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi.{SMSet, SMIncrement, SMUpdate}

trait SMUpdateExtensions {
  implicit class SMUpdateExtensions(update: SMUpdate) {
    def tup: (String, String) = {
      update match {
        case inc: SMIncrement => s"${inc.getField}[inc]" -> inc.getValue.getValue.toString
        case set: SMSet => set.getField -> set.getValue.getValue.toString
      }
    }

  }
}
