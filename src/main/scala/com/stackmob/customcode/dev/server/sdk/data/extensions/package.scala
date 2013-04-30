package com.stackmob.customcode.dev
package server
package sdk
package data

import com.stackmob.sdkapi.SMValue

package object extensions
  extends SMValueExtensions
  with SMStringExtensions
  with SMPrimitiveExtensions
  with SMListExtensions
  with SMObjectExtensions
  with SMUpdateExtensions
  with StackMobQueryExtensions {
  private[extensions] def checkDepth[T](depth: Int)(fn: => T) = {
    if(depth > maxDepth) {
      throw new SMValueDepthLimitReached(maxDepth)
    } else {
      fn
    }
  }

  class UnsupportedSMValueException(smValue: SMValue[_]) extends Exception(s"unsupported SMValue ${smValue.getClass.getName}")
}
