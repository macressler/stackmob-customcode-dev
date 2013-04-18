package com.stackmob.customcode.localrunner.sdk

import org.specs2.Specification

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 4:03 PM
 */
class CallLimitationSpecs extends Specification { def is =
  "CallLimitationSpecs".title                                                                                           ^ end ^
  "CallLimitation tracks the number of calls to a method or some methods, and then throws if over a high watermark"     ^ end ^
                                                                                                                        end
  //TODO: make sure an op only executes once in all cases

}
