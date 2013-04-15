package com.stackmob.customcode.localrunner.sdk.data

import com.stackmob.sdkapi.{SMSet, SMIncrement, SMUpdate}
import SMValueUtils._

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.data
 *
 * User: aaron
 * Date: 4/15/13
 * Time: 2:05 PM
 */
object SMUpdateUtils {
  implicit class SMUpdateW(update: SMUpdate) {
    def tup: (String, String) = {
      update match {
        case inc: SMIncrement => "%s[inc]".format(inc.getField) -> inc.getValue.underlying.toString
        case set: SMSet => set.getField -> set.getValue.underlying.toString
      }
    }

  }
}
