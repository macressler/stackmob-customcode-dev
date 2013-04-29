package com.stackmob.customcode.dev
package server
package sdk
package data

package object extensions
  extends SMValueExtensions
  with SMStringExtensions
  with SMPrimitiveExtensions
  with SMListExtensions
  with SMObjectExtensions
  with SMUpdateExtensions
  with StackMobQueryExtensions {
  private[extensions] def checkDepth[T](depth: Int)(fn: => T) = {
    if(depth >= maxDepth) {
      throw new SMValueDepthLimitReached(maxDepth)
    } else {
      fn
    }
  }
}
