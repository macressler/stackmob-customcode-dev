package com.stackmob.customcode.dev
package test
package server.sdk.data

import org.specs2.{ScalaCheck, Specification}
import com.stackmob.sdkapi._
import com.stackmob.customcode.dev.server.sdk.data.extensions._
import net.liftweb.json._
import org.specs2.matcher.{MatchResult, Matcher, Expectable}
import scala.reflect.ClassTag
import collection.JavaConverters._
import org.scalacheck.Prop.forAll
import scala.util.Try
import com.stackmob.customcode.dev.server.SMValueDepthLimitReached

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data
 *
 * User: aaron
 * Date: 4/25/13
 * Time: 4:26 PM
 */
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
    private def beInstanceAndEqual[T: ClassTag](value: T) = new Matcher[Any] {
      override def apply[S <: Any](x: Expectable[S]): MatchResult[S] = {
        val inst = x.value must beAnInstanceOf[T]
        val equal = x.value must beEqualTo(value)
        inst and equal
      }
    }

    def smInt = {
      val smInt = new SMInt(1L)
      smInt.toJValue must beInstanceAndEqual[JInt](JInt(smInt.getValue.toInt))
    }
    def smLong = {
      val smLong = new SMLong(1L)
      smLong.toJValue must beInstanceAndEqual[JInt](JInt(smLong.getValue.toInt))
    }
    def smDouble = {
      val smDouble = new SMDouble(1D)
      smDouble.toJValue must beInstanceAndEqual[JDouble](JDouble(smDouble.getValue))
    }
    def smBoolean = {
      val smBoolean = new SMBoolean(true)
      smBoolean.toJValue must beInstanceAndEqual[JBool](JBool(smBoolean.getValue))
    }
    def smString = {
      val smString = new SMString("abc")
      smString.toJValue must beInstanceAndEqual[JString](JString(smString.getValue))
    }
    def smList = forAll(genUnderMaxDepth) { depth =>
      val smList = SMListTestUtils.createNested(depth, baseSMList)
      val expectedJArray = createNestedJArray(depth, JArray(baseJValueList))
      smList.toJValue() must beInstanceAndEqual[JArray](expectedJArray)
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
      smObject.toJValue() must beInstanceAndEqual[JObject](expectedJObject)
    }
    def smObjectThrow = forAll(genOverMaxDepth) { depth =>
      val nested = SMObjectTestUtils.createNested(depth, "test-base-key", baseSMObject)
      val resultJValue = nested.toJValue()
      Try(resultJValue).toEither must beLeft.like {
        case t => t must beAnInstanceOf[SMValueDepthLimitReached]
      }
    }
  }
}
