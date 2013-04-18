package com.stackmob.customcode.localrunner.sdk.data

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.data
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 4:22 PM
 */
class TooManyDataServiceCallsException(max: Int, calls: Seq[String]) extends Exception("""
    You've made more than %d calls to DataService in this request. Doing so will cause your custom code to be
    slow and possibly take too many resources. Please reduce the number of calls you're making.
    Here are the calls that you've made, in order:
    %s""".format(max, calls.mkString("\n")))

object TooManyDataServiceCallsException {
  def apply(max: Int, calls: Seq[String]) = new TooManyDataServiceCallsException(max, calls)
}