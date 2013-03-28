package com.stackmob.customcode.localrunner.sdk.data

import com.stackmob.sdkapi.SMValue

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.data
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 5:08 PM
 */
trait SMValueW[T] {
  protected def smValue: SMValue[T]
  def underlying = smValue.getValue
}