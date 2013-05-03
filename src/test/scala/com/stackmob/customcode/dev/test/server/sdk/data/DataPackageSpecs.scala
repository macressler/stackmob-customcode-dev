package com.stackmob.customcode.dev
package test
package server
package sdk
package data

import org.specs2.{ScalaCheck, Specification}
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.sdkapi._
import org.specs2.matcher.MatchResult
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen
import com.stackmob.customcode.dev.test.CustomMatchers
import com.stackmob.customcode.dev.server.SMValueDepthLimitReached
import scala.reflect.ClassTag
import collection.JavaConverters._
import scala.util.Try

class DataPackageSpecs extends Specification with ScalaCheck with CustomMatchers { def is =
  "DataPackageSpecs".title                                                                                              ^ end ^
  "DataPackageSpecs test the various functionality in the com.stackmob.customcode.dev.server.sdk.data._ package obj"    ^ end ^
  "smBody should work properly"                                                                                         ! smBodyTest ^ end ^
  "smValue should"                                                                                                      ^
    "work properly for booleans"                                                                                        ! checkSMValue(true)(new SMBoolean(true)) ^
    "work properly for Longs"                                                                                           ! checkSMValue(1L)(new SMInt(1L)) ^
    "work properly for Doubles"                                                                                         ! checkSMValue(1D)(new SMDouble(1D)) ^
    "work properly for Ints"                                                                                            ! checkSMValue(1)(new SMInt(1L)) ^
    "work properly for Strings"                                                                                         ! checkSMValue("a")(new SMString("a")) ^
    "work properly for lists of primitives"                                                                             ! checkSMValuePrimitiveList ^
    "work properly for nested lists"                                                                                    ! checkSMValue(List(List("a"))) {
      new SMList(new SMList(new SMString("a")))
    } ^
    "throw for lists nested too deeply"                                                                                 ! checkSMValueThrows[SMValueDepthLimitReached] {
      List(List(List(List(List("a")))))
    } ^
    "work properly for maps of primitives"                                                                              ! checkSMValue(Map("a" -> "b")) {
      new SMObject(Map[String, SMValue[_]]("a" -> new SMString("b")).asJava)
    } ^
    "work properly for nested maps"                                                                                     ! checkSMValue(Map("a" -> Map("a" -> "b"))) {
      new SMObject(
        Map[String, SMValue[_]]("a" ->
          new SMObject(
            Map[String, SMValue[_]]("a" ->
              new SMString("b")
            ).asJava
          )
        ).asJava
      )
    } ^
    "throw for maps nested too deeply"                                                                                  ! checkSMValueThrows[SMValueDepthLimitReached] {
      Map("a" -> Map("a" -> Map("a" -> Map("a" -> Map("a" -> Map("a" -> "b"))))))
    } ^
  end ^
  "smOptions should"                                                                                                    ^
    "work correctly with expandDepth"                                                                                   ! SMOptions().expandDepth ^
    "work correctly with fields"                                                                                        ! SMOptions().fields ^
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
  private def checkSMValue(t: Any)(smT: SMValue[_]): MatchResult[Any] = {
    val res: Any = smValue(t)
    res must beEqualTo(smT: Any)
  }

  private def checkSMValueThrows[T <: Throwable: ClassTag](t: Any): MatchResult[Any] = {
    Try(smValue(t): Any).toEither must beThrowableInstance[T]
  }

  private def checkSMValuePrimitiveList = {
    val origList = List[Object](new Integer(1), "a")
    val expectedSMValues: List[SMValue[_]] = List(new SMInt(1L), new SMString("a"))
    val expectedSMList = new SMList(expectedSMValues.asJava)
    checkSMValue(origList)(expectedSMList)
  }

  private case class SMOptions() {
    def expandDepth = {
      smOptions(3).getExpandDepth must beEqualTo(3)
    }
    def fields = forAll(Gen.listOf(Gen.alphaStr)) { fields =>
      smOptions(1, Some(fields)).getSelection.asScala must haveTheSameElementsAs(fields)
    }
  }
}
