package com.stackmob.customcode.dev
package test
package server.sdk.data

import org.specs2.{ScalaCheck, Specification}
import com.stackmob.sdkapi._
import com.stackmob.customcode.dev.server.sdk.data.extensions._
import scalaz._
import Scalaz._
import net.liftweb.json._
import collection.JavaConverters._
import org.scalacheck.Prop.forAll
import scala.util.Try
import com.stackmob.customcode.dev.server.SMValueDepthLimitReached

class SMValueExtensionsSpecs extends Specification with ScalaCheck { def is =
  "SMValueExtensionsSpecs".title                                                                                             ^ end ^
  "SMValueUtils is a class extension to convert SMValues to other formats (Object, JValue, etc)"                        ^ end ^
  "toJValue should"                                                                                                     ^
    "work properly for SMInt"                                                                                           ! ToJValue().smInt ^
    "work properly for SMLong"                                                                                          ! ToJValue().smLong ^
    "work properly for SMDouble"                                                                                        ! ToJValue().smDouble ^
    "work properly for SMBoolean"                                                                                       ! ToJValue().smBoolean ^
    "work properly for SMString"                                                                                        ! ToJValue().smString ^
    "work properly for SMList"                                                                                          ! ToJValue().smList ^
    "throw for a deeply nested SMList"                                                                                  ! ToJValue().smListThrow ^
    "work properly for SMObject"                                                                                        ! ToJValue().smObject ^
    "throw for a deeply nested SMObject"                                                                                ! ToJValue().smObjectThrow ^
  end ^
  "toObject should"                                                                                                     ^
    "work properly for any SMPrimitive"                                                                                 ! pending ^
    "work properly for an SMString"                                                                                     ! pending ^
    "work properly for an SMList"                                                                                       ! pending ^
    "throw for a deeply nested SMList"                                                                                  ! pending ^
    "work properly for an SMObject"                                                                                     ! pending ^
    "throw for a deeply nested SMObject"                                                                                ! pending ^
  end

  sealed private trait Base {
    protected lazy val booleanValue = true
    protected lazy val stringValue = "testSMString"
    protected lazy val smBooleanValue = new SMBoolean(booleanValue)
    protected lazy val smStringValue = new SMString(stringValue)
    protected lazy val jBoolValue = JBool(booleanValue)
    protected lazy val jStringValue = JString(stringValue)
    protected lazy val baseKey = "testBaseKey"
    protected lazy val baseMap = Map[String, SMValue[_]](baseKey -> smBooleanValue, baseKey -> smStringValue)
    protected lazy val baseList = List(smBooleanValue, smStringValue)
    protected lazy val baseSMObject = new SMObject(baseMap.asJava)
    protected lazy val baseSMList = new SMList(baseList.asJava)
    protected lazy val baseJValueList = List(jStringValue, jBoolValue)
    protected lazy val baseJFieldList = baseJValueList.map { jValue =>
      JField(baseKey, jValue)
    }
  }

  private case class ToJValue() extends Base {
    def smInt = {
      val smInt = new SMInt(1L)
      smInt.toJValue.values must beEqualTo(smInt.getValue.toInt)
    }
    def smLong = {
      val smLong = new SMLong(1L)
      smLong.toJValue.values must beEqualTo(smLong.getValue.toInt)
    }
    def smDouble = {
      val smDouble = new SMDouble(1D)
      smDouble.toJValue.values must beEqualTo(smDouble.getValue)
    }
    def smBoolean = {
      val smBoolean = new SMBoolean(true)
      smBoolean.toJValue.values must beEqualTo(smBoolean.getValue)
    }
    def smString = {
      val smString = new SMString("abc")
      smString.toJValue.values must beEqualTo(smString.getValue)
    }
    def smList = forAll(genUnderMaxDepth) { depth =>
      val smList = SMListTestUtils.createNested(depth, baseSMList)
      val expectedList = createNestedJArray(depth, JArray(baseJValueList)).toList
      val resJValue = smList.toJValue()
      (resJValue.toList <|*|> expectedList) must beSome.like {
        case tup => {
          val (list1, list2) = tup
          list1 must haveTheSameElementsAs(list2)
        }
      }
    }
    def smListThrow = forAll(genOverMaxDepth) { depth =>
      val nested = SMListTestUtils.createNested(depth, baseSMList)
      Try(nested.toJValue()).toEither must beLeft.like {
        case t => t must beAnInstanceOf[SMValueDepthLimitReached]
      }
    }
    def smObject = forAll(genUnderMaxDepth) { depth =>
      val baseMap: Map[String, SMValue[_]] = Map(baseKey -> smStringValue, baseKey -> smBooleanValue)
      val baseSMObject = new SMObject(baseMap.asJava)
      val smObject = SMObjectTestUtils.createNested(depth, baseKey, baseSMObject)
      val expectedJObject = createNestedJObject(depth, JObject(baseJFieldList), baseKey)
      val resJValue = smObject.toJValue()
      (resJValue.toMap <|*|> expectedJObject.toMap) must beSome.like {
        case tup => {
          val (map1, map2) = tup
          map1 must haveTheSameElementsAs(map2)
        }
      }
    }
    def smObjectThrow = forAll(genOverMaxDepth) { depth =>
      val nested = SMObjectTestUtils.createNested(depth, "test-base-key", baseSMObject)
      Try(nested.toJValue()).toEither must beLeft.like {
        case t => t must beAnInstanceOf[SMValueDepthLimitReached]
      }
    }
  }
}
