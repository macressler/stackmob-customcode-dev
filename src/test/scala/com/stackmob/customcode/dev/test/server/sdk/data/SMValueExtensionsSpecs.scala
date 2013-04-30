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
    "work properly for SMObject"                                                                                        ! pending ^ //ToJValue().smObject ^
    "throw for a deeply nested SMObject"                                                                                ! pending ^ //ToJValue().smObjectThrow ^
  end ^
  "toObject should"                                                                                                     ^
    "work properly for any SMPrimitive"                                                                                 ! pending ^
    "work properly for an SMString"                                                                                     ! pending ^
    "work properly for an SMList"                                                                                       ! pending ^
    "throw for a deeply nested SMList"                                                                                  ! pending ^
    "work properly for an SMObject"                                                                                     ! pending ^
    "throw for a deeply nested SMObject"                                                                                ! pending ^
  end

  private sealed trait Base {

  }

  private case class ToJValue() {
    private def beInstanceAndEqual[T: ClassTag](value: T) = new Matcher[Any] {
      override def apply[S <: Any](x: Expectable[S]): MatchResult[S] = {
        val inst = x.value must beAnInstanceOf[T]
        val equal = x.value must beEqualTo(value)
        inst and equal
      }
    }

    def smInt = {
      val smInt = new SMInt(1L)
      smInt.toJValue() must beInstanceAndEqual[JInt](JInt(smInt.getValue.toInt))
    }
    def smLong = {
      val smLong = new SMLong(1L)
      smLong.toJValue() must beInstanceAndEqual[JInt](JInt(smLong.getValue.toInt))
    }
    def smDouble = {
      val smDouble = new SMDouble(1D)
      smDouble.toJValue() must beInstanceAndEqual[JDouble](JDouble(smDouble.getValue))
    }
    def smBoolean = {
      val smBoolean = new SMBoolean(true)
      smBoolean.toJValue() must beInstanceAndEqual[JBool](JBool(smBoolean.getValue))
    }
    def smString = {
      val smString = new SMString("abc")
      smString.toJValue() must beInstanceAndEqual[JString](JString(smString.getValue))
    }
    def smList = {
      val smString = new SMString("abc")
      val smBoolean = new SMBoolean(true)
      val smValueList = List(smString, smBoolean)
      val smList = new SMList(smValueList.asJava)
      val expectedJValueList = List(JString(smString.getValue), JBool(smBoolean.getValue))
      smList.toJValue() must beInstanceAndEqual[JArray](JArray(expectedJValueList))
    }
    def smListThrow = forAll(genOverMaxDepth) { depth =>
      val nested = SMListTestUtils.createNested(depth)
      Try(nested.toJValue()).toEither must beLeft.like {
        case t => t must beAnInstanceOf[SMValueDepthLimitReached]
      }
    }
//    def smObject = {
//      val smString = new SMString("abc")
//      val smBoolean = new SMBoolean(true)
//      val smValueMap = Map[String, SMValue[_]]("string" -> smString, "boolean" -> smBoolean)
//      val jValueMap = Map("string" -> JString(smString.getValue), "boolean" -> JBool(smBoolean.getValue))
//      val smObject = new SMObject(smValueMap.asJava)
//      val expectedJObject = {
//        val expectedFields = jValueMap.map { tup =>
//          val (key, value) = tup
//          JField(key, value)
//        }.toList
//        JObject(expectedFields)
//      }
//      smObject.toJValue() must beInstanceAndEqual[JObject](expectedJObject)
//    }
//    def smObjectThrow = forAll(genOverMaxDepth) { depth =>
//      val nested = SMObjectTestUtils.createNested(depth)
//      val resultJValue = nested.toJValue()
//      Try(resultJValue).toEither must beLeft.like {
//        case t => t must beAnInstanceOf[SMValueDepthLimitReached]
//      }
//    }
  }
}
