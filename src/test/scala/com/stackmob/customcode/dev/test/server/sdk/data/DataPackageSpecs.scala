package com.stackmob.customcode.dev.test.server.sdk.data

import org.specs2.Specification
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.sdkapi._
import collection.JavaConverters._
import org.specs2.matcher.MatchResult

class DataPackageSpecs extends Specification { def is =
  "DataPackageSpecs".title                                                                                              ^ end ^
  "DataPackageSpecs test the various functionality in the com.stackmob.customcode.dev.server.sdk.data._ package obj"    ^ end ^
  "smBody should work properly"                                                                                         ! smBodyTest ^ end ^
  "smValue should"                                                                                                      ^
    "work properly for booleans"                                                                                        ! checkSMValue(true, new SMBoolean(true)) ^
    "work properly for Longs"                                                                                           ! checkSMValue(1L, new SMInt(1L)) ^
    "work properly for Doubles"                                                                                         ! checkSMValue(1D, new SMDouble(1D)) ^
    "work properly for Ints"                                                                                            ! checkSMValue(1, new SMInt(1L)) ^
    "work properly for Strings"                                                                                         ! checkSMValue("a", new SMString("a")) ^
    "work properly for lists of primitives"                                                                             ! checkSMValuePrimitiveList ^
    "work properly for nested lists"                                                                                    ! pending ^
    "throw for lists nested too deeply"                                                                                 ! pending ^
    "work properly for maps of primitives"                                                                              ! pending ^
    "work properly for nested maps"                                                                                     ! pending ^
    "throw for maps nested too deeply"                                                                                  ! pending ^
                                                                                                                        end ^
                                                                                                                        end
  private def smBodyTest = {
    val smInc = new SMIncrement("testInc", 1)
    val smSet = new SMSet("testSet", new SMInt(1))
    val smUpdates = List[SMUpdate](smInc, smSet)
    val res = smBody(smUpdates)
    val expectedMap = Map(s"${smInc.getField}[inc]" -> smInc.getValue.getValue.toString,
      smSet.getField -> smSet.getValue.getValue.toString)
    res must haveTheSameElementsAs(expectedMap)
  }

  /**
   * check that a raw value is converted to an SMValue as expected
   * @param t the raw value to be converted
   * @param smT the SMValue that should result from the conversion
   * @return the MatchResult representing whether the conversion succeeded
   * @note because SMValue is invariant in its type {{T}}, we have to cast everything up
   *       to {{Any}} in order to make this code compile
   */
  def checkSMValue(t: Any, smT: SMValue[_]): MatchResult[Any] = {
    val res: Any = smValue(t)
    res must beEqualTo(smT: Any)
  }

  def checkSMValuePrimitiveList = {
    val origList = List[Object](new Integer(1), "a")
    val expectedSMValues: List[SMValue[_]] = List(new SMInt(1L), new SMString("a"))
    val expectedSMList = new SMList(expectedSMValues.asJava)
    checkSMValue(origList, expectedSMList)
  }
}
