package com.stackmob.customcode.dev
package test
package server.sdk.data

import org.specs2.{ScalaCheck, Specification}
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.server.sdk.data.StackMobQueryUtils
import com.stackmob.sdk.api.{StackMobGeoPoint, StackMobQuery}
import com.stackmob.sdkapi._
import collection.JavaConverters._
import com.stackmob.customcode.dev.server.sdk._
import org.scalacheck.Prop._
import org.scalacheck.Gen
import StackMobQueryUtils._
import scala.util.Try
import com.stackmob.customcode.dev.server.{SMConditionDepthLimitReached, maxDepth}
import org.mockito.Matchers.{eq => mockitoEq}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data
 *
 * User: aaron
 * Date: 4/24/13
 * Time: 2:21 PM
 */
class StackMobQueryUtilsSpecs extends Specification with Mockito with ScalaCheck { def is =
  "StackMobQueryUtilsSpecs".title                                                                                       ^ end ^
  "StackMobQueryUtils has implicit classes to support building StackMobQuery objects from SMConditions"                 ^ end ^
  "addCondition should work correctly with"                                                                             ^
    "SMNull(true)"                                                                                                      ! AddCondition().smIsNull ^
    "SMNull(false)"                                                                                                     ! AddCondition().smIsNotNull ^
    "SMIn"                                                                                                              ! AddCondition().smIn ^
    "SMEquals"                                                                                                          ! AddCondition().smEquals ^
    "SMNotEqual"                                                                                                        ! AddCondition().smNotEqual ^
    "SMGreater"                                                                                                         ! AddCondition().smGreater ^
    "SMGreaterThanOrEqual"                                                                                              ! AddCondition().smGreaterOrEqual ^
    "SMLess"                                                                                                            ! AddCondition().smLess ^
    "SMLessOrEqual"                                                                                                     ! AddCondition().smLessOrEqual ^
    "SMAnd"                                                                                                             ! AddCondition().smAnd ^
                                                                                                                        end ^
  "addCondition should throw for"                                                                                       ^
    "SMOr"                                                                                                              ! Throw().smOr ^
    "SMAnd, over the depth limit"                                                                                       ! Throw().smAndOverDepthLimit ^
                                                                                                                        end

  private sealed trait Base {
    protected lazy val origQuery1: StackMobQuery = {
      mock[StackMobQuery].as("mock StackMobQuery").defaultAnswer { i =>
        origQuery2
      }
    }
    protected val origQuery2: StackMobQuery = {
      mock[StackMobQuery].as("mock2 StackMobQuery").defaultAnswer { i =>
        origQuery1
      }
    }

    protected val field = "field-name"
    protected val value = "field-value"
    protected val genLatitude = Gen.posNum[Double]
    protected val genLongitude = Gen.posNum[Double]
    protected val genDistance = Gen.oneOf(Gen.posNum[Double], Gen.value(0D))

    //don't use large numbers here because they'll affect how deep the nested SMAnds will be generated.
    //affects runtime & space
    protected val genOverMaxDepth = Gen.choose(maxDepth + 1, maxDepth + 10)

    protected val isNull = new SMIsNull(field, new SMBoolean(true))
    protected val isEqual = new SMEquals(field, new SMString(value))
    protected val clauses = List(isNull, isEqual)
  }

  private case class AddCondition() extends Base {
    def smIsNull = {
      val isNull = new SMIsNull(field, new SMBoolean(true))
      origQuery1.addSMCondition(isNull)
      (there was one(origQuery1).fieldIsNull(field)) and
      (there was no(origQuery1).fieldIsNotNull(field))
    }

    def smIsNotNull = {
      val isNotNull = new SMIsNull(field, new SMBoolean(false))
      origQuery1.addSMCondition(isNotNull)
      (there was no(origQuery1).fieldIsNull(field)) and
      (there was one(origQuery1).fieldIsNotNull(field))
    }

    def smIn = {
      val smString = new SMString("hello world")
      val smBool = new SMBoolean(true)
      val valuesList = List(smString, smBool)
      val smIn = new SMIn(field, valuesList.asJava)
      origQuery1.addSMCondition(smIn)
      there was one(origQuery1).fieldIsIn(mockitoEq(field), any[JList[String]])
    }

    def smEquals = {
      val smEquals = new SMEquals(field, new SMString(value))
      origQuery1.addSMCondition(smEquals)
      there was one(origQuery1).fieldIsEqualTo(field, value)
    }

    def smNotEqual = {
      val smNotEqual = new SMNotEqual(field, new SMString(value))
      origQuery1.addSMCondition(smNotEqual)
      there was one(origQuery1).fieldIsNotEqual(field, value)
    }

    def smGreater = {
      val smGreater = new SMGreater(field, new SMString(value))
      origQuery1.addSMCondition(smGreater)
      there was one(origQuery1).fieldIsGreaterThan(field, value)
    }

    def smGreaterOrEqual = {
      val smGreaterOrEqual = new SMGreaterOrEqual(field, new SMString(value))
      origQuery1.addSMCondition(smGreaterOrEqual)
      there was one(origQuery1).fieldIsGreaterThanOrEqualTo(field, value)
    }

    def smLess = {
      val smLess = new SMLess(field, new SMString(value))
      origQuery1.addSMCondition(smLess)
      there was one(origQuery1).fieldIsLessThan(field, value)
    }

    def smLessOrEqual = {
      val smLessOrEqual = new SMLessOrEqual(field, new SMString(value))
      origQuery1.addSMCondition(smLessOrEqual)
      there was one(origQuery1).fieldIslessThanOrEqualTo(field, value)
    }

    def smNear = forAll(genLatitude, genLongitude, genDistance) { (latitude, longitude, distance) =>
      val smNear = new SMNear(field, new SMDouble(latitude), new SMDouble(longitude), new SMDouble(distance))
      origQuery1.addSMCondition(smNear)
      val expectedGeoPoint = new StackMobGeoPoint(smNear.getLon.getValue, smNear.getLat.getValue)
      there was one(origQuery1).fieldIsNear(field, expectedGeoPoint)
    }

    def smWithin = forAll(genLatitude, genLongitude, genDistance) { (latitude, longitude, distance) =>
      val smWithin = new SMWithin(field, new SMDouble(latitude), new SMDouble(longitude), new SMDouble(distance))
      origQuery1.addSMCondition(smWithin)
      val expectedGeoPoint = new StackMobGeoPoint(smWithin.getLon.getValue, smWithin.getLat.getValue)
      val km = StackMobGeoPoint.radiansToKm(distance)
      there was one(origQuery1).fieldIsWithinRadiusInKm(field, expectedGeoPoint, km)
    }

    def smWithinBox = forAll(genLatitude, genLongitude, genLatitude, genDistance) {
      (latLL, lonLL, latUR, distance) =>
      val smWithinBox = new SMWithinBox(field,
        new SMDouble(latLL),
        new SMDouble(lonLL),
        new SMDouble(latUR),
        new SMDouble(distance))
      origQuery1.addSMCondition(smWithinBox)
      val expectedLL = new StackMobGeoPoint(smWithinBox.getLonLL.getValue, smWithinBox.getLatLL.getValue)
      val expectedUR = new StackMobGeoPoint(smWithinBox.getLonUR.getValue, smWithinBox.getLatUR.getValue)
      there was one(origQuery1).fieldIsWithinBox(field, expectedLL, expectedUR)
    }

    def smAnd = {
      val smAnd = new SMAnd(clauses.asJava)
      origQuery1.addSMCondition(smAnd)
      val nullCalled = there was one(origQuery1).fieldIsNull(field)
      val equalCalled = there was one(origQuery2).fieldIsEqualTo(field, value)
      nullCalled and equalCalled
    }
  }

  private case class Throw() extends Base {
    def smOr = {
      val smOr = new SMOr(clauses.asJava)
      Try(origQuery1.addSMCondition(smOr)).toEither must beLeft.like {
        case t => t must beAnInstanceOf[UnsupportedOperationException]
      }
    }

    def smAndOverDepthLimit = forAll(genOverMaxDepth) { depth =>
      val startingSMAnd = new SMAnd(clauses.asJava)
      val nestedSMAnd = (0 until depth).foldLeft(startingSMAnd) { (agg, cur) =>
        new SMAnd(List[SMCondition](agg).asJava)
      }
      Try(origQuery1.addSMCondition(nestedSMAnd)).toEither must beLeft.like {
        case t => t must beAnInstanceOf[SMConditionDepthLimitReached]
      }
    }
  }

}
