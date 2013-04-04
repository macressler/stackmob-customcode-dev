package com.stackmob.customcode.localrunner.sdk.data

import com.stackmob.sdkapi.{SMValue, SMSet, SMIncrement, SMUpdate}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.data
 *
 * User: aaron
 * Date: 4/3/13
 * Time: 3:09 PM
 */
trait SMUpdateW {
  protected def update: SMUpdate

  def tup: (String, String) = {
    update match {
      case inc: SMIncrement => "%s[inc]".format(inc.getField) -> inc.getValue.underlying.toString
      case set: SMSet => set.getField -> set.getValue.underlying.toString
    }
  }

}
