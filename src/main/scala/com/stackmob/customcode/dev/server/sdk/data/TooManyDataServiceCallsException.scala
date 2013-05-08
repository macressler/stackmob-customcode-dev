package com.stackmob.customcode.dev.server.sdk.data

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.sdk.data
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 4:22 PM
 */
class TooManyDataServiceCallsException(max: Int, calls: Seq[String]) extends Exception {
  val callList = calls.mkString("\n")
  s"""
    You've made more than $max calls to DataService in this request. Doing so will cause your custom code to be
    slow and possibly take too many resources. Please reduce the number of calls you're making.
    Here are the calls that you've made, in order:
    $callList"""
}

object TooManyDataServiceCallsException {
  def apply(max: Int, calls: Seq[String]) = new TooManyDataServiceCallsException(max, calls)
}