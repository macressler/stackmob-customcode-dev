package com.stackmob.customcode.dev.test.server.sdk.data

import org.specs2.Specification
import com.stackmob.customcode.dev.server.sdk.data.SMUpdateUtils._
import com.stackmob.sdkapi.{SMSet, SMInt, SMIncrement}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data
 *
 * User: aaron
 * Date: 4/25/13
 * Time: 4:06 PM
 */
class SMUpdateUtilsSpecs extends Specification { def is =
  "SMUpdateUtilsSpecs".title                                                                                            ^ end ^
  "SMUpdateUtils is a class extension designed to convert an SMUpdate to a tuple representing a PUT body"               ^ end ^
  "an SMIncrement should work properly"                                                                                 ! smIncrement ^ end ^
  "an SMSet should work properly"                                                                                       ! smSet ^ end ^
                                                                                                                        end
  private lazy val testField = "test-field"
  private lazy val testValue = 1L

  private def smIncrement = {
    val smInc = new SMIncrement(testField, new SMInt(testValue))
    val (field, value) = smInc.tup
    val expectedField = s"$testField[inc]"
    val fieldRes = field must beEqualTo(expectedField)
    val valueRes = value must beEqualTo(testValue.toString)
    fieldRes and valueRes
  }

  private def smSet = {
    val smSet = new SMSet(testField, new SMInt(testValue))
    val (field, value) = smSet.tup
    val fieldRes = field must beEqualTo(testField)
    val valueRes = value must beEqualTo(testValue.toString)
    fieldRes and valueRes
  }

}
