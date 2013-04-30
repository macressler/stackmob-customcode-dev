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
import com.stackmob.customcode.dev.server.sdk.{JavaList, JavaMap}

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
    "work properly for any SMPrimitive"                                                                                 ! ToObject().smPrimitive ^
    "work properly for an SMString"                                                                                     ! ToObject().smString ^
    "work properly for an SMList"                                                                                       ! ToObject().smList ^
    "throw for a deeply nested SMList"                                                                                  ! ToObject().smListThrow ^
    "work properly for an SMObject"                                                                                     ! ToObject().smObject ^
    "throw for a deeply nested SMObject"                                                                                ! ToObject().smObjectThrow ^
  end

  sealed private trait Base {
    protected lazy val longValue = 1L
    protected lazy val smIntValue = new SMInt(longValue)
    protected lazy val smLongValue = new SMLong(longValue)
    protected lazy val jIntValue = JInt(longValue)

    protected lazy val doubleValue = 1D
    protected lazy val smDoubleValue = new SMDouble(doubleValue)
    protected lazy val jDoubleValue = JDouble(doubleValue)

    protected lazy val booleanValue = true
    protected lazy val smBooleanValue = new SMBoolean(booleanValue)
    protected lazy val jBoolValue = JBool(booleanValue)

    protected lazy val stringValue = "testSMString"
    protected lazy val smStringValue = new SMString(stringValue)
    protected lazy val jStringValue = JString(stringValue)

    protected lazy val baseKey = "testBaseKey"
    protected lazy val baseMap = Map[String, Any](baseKey -> booleanValue)
    protected lazy val baseList = List(longValue, doubleValue, booleanValue, stringValue)

    protected lazy val baseSMValueMap = Map[String, SMValue[_]](baseKey -> smBooleanValue)
    protected lazy val baseSMValueList: List[SMValue[_]] = List(smLongValue, smDoubleValue, smBooleanValue, smStringValue)
    protected lazy val baseSMObject = new SMObject(baseSMValueMap.asJava)
    protected lazy val baseSMList = new SMList(baseSMValueList.asJava)
    protected lazy val expectedJArray = JArray(List(jIntValue, jDoubleValue, jBoolValue, jStringValue))
    protected lazy val expectedJObject = {
      val jFields = List(JField(baseKey, JBool(booleanValue)))
      JObject(jFields)
    }

    protected def throwValueLimitReached = beLeft[Throwable].like {
      case t => t must beAnInstanceOf[SMValueDepthLimitReached]
    }
  }

  private case class ToJValue() extends Base {
    def smInt = {
      smIntValue.toJValue.values must beEqualTo(smIntValue.getValue.toInt)
    }
    def smLong = {
      smLongValue.toJValue.values must beEqualTo(smLongValue.getValue.toInt)
    }
    def smDouble = {
      smDoubleValue.toJValue.values must beEqualTo(smDoubleValue.getValue)
    }
    def smBoolean = {
      smBooleanValue.toJValue.values must beEqualTo(smBooleanValue.getValue)
    }
    def smString = {
      smStringValue.toJValue.values must beEqualTo(smStringValue.getValue)
    }
    def smList = forAll(genUnderMaxDepth) { depth =>
      val smList = SMListTestUtils.createNested(depth, baseSMList)
      val nestedJArray = createNestedJArray(depth, expectedJArray)
      val resJValue = smList.toJValue()
      (resJValue.toList <|*|> nestedJArray.toList) must beSome.like {
        case tup => {
          val (list1, list2) = tup
          list1 must haveTheSameElementsAs(list2)
        }
      }
    }
    def smListThrow = forAll(genOverMaxDepth) { depth =>
      val nested = SMListTestUtils.createNested(depth, baseSMList)
      Try(nested.toJValue()).toEither must throwValueLimitReached
    }
    def smObject = forAll(genUnderMaxDepth) { depth =>
      val smObject = SMObjectTestUtils.createNested(depth, baseKey, baseSMObject)
      val nestedJObject = createNestedJObject(depth, expectedJObject, baseKey)
      val resJValue = smObject.toJValue()
      (resJValue.toMap <|*|> nestedJObject.toMap) must beSome.like {
        case tup => {
          val (map1, map2) = tup
          map1 must haveTheSameElementsAs(map2)
        }
      }
    }
    def smObjectThrow = forAll(genOverMaxDepth) { depth =>
      val nested = SMObjectTestUtils.createNested(depth, "test-base-key", baseSMObject)
      Try(nested.toJValue()).toEither must throwValueLimitReached
    }
  }

  private case class ToObject() extends Base {
    def smPrimitive = {
      val intRes = smIntValue.toObject must beEqualTo(smIntValue.getValue)
      val longRes = smLongValue.toObject must beEqualTo(smLongValue.getValue)
      val boolRes = smBooleanValue.toObject must beEqualTo(smBooleanValue.getValue)
      val doubleRes = smDoubleValue.toObject must beEqualTo(smDoubleValue.getValue)
      intRes and longRes and boolRes and doubleRes
    }
    def smString = {
      smStringValue.toObject must beEqualTo(smStringValue.getValue)
    }

    private def unwindToList(obj: Object, targetDepth: Int, curDepth: Int = 0): Option[JavaList[_]] = {
      obj match {
        case lst: JavaList[_] if curDepth == targetDepth => {
          Some(lst)
        }
        case lst: JavaList[_] => {
          Option(lst.get(0)).flatMap { firstElt =>
            firstElt match {
              case innerLst: JavaList[_] => {
                unwindToList(innerLst, targetDepth, curDepth + 1)
              }
              case _ => None
            }
          }
        }
        case _ => {
          None
        }
      }
    }

    private def unwindToMap(obj: Object, nestKey: String, targetDepth: Int, curDepth: Int = 0): Option[JavaMap[String, _]] = {
      obj match {
        case map: JavaMap[String, _] if curDepth == targetDepth => {
          Some(map)
        }
        case map: JavaMap[String, _] => {
          Option(map.get(nestKey)).flatMap { value =>
            value match {
              case innerMap: JavaMap[String, _] => {
                unwindToMap(innerMap, nestKey, targetDepth, curDepth + 1)
              }
              case _ => None
            }
          }
        }
        case _ => None
      }

    }

    def smList = forAll(genUnderMaxDepth) { depth =>
      val smList = SMListTestUtils.createNested(depth, baseSMList)
      val unwound = unwindToList(smList.toObject(), depth)
      unwound must beSome.like {
        case lst => {
          val scalaList = lst.asScala.toList
          val expectedList = baseList
          scalaList must haveTheSameElementsAs(expectedList)
        }
      }
    }
    def smListThrow = forAll(genOverMaxDepth) { depth =>
      val smList = SMListTestUtils.createNested(depth, baseSMList)
      Try(smList.toObject()).toEither must throwValueLimitReached
    }
    def smObject = forAll(genUnderMaxDepth) { depth =>
      val smObject = SMObjectTestUtils.createNested(depth, baseKey, baseSMObject)
      val unwound = unwindToMap(smObject.toObject(), baseKey, depth)
      unwound must beSome.like {
        case map => {
          val scalaMap = map.asScala
          val expectedMap = baseMap
          scalaMap must haveTheSameElementsAs(expectedMap)
        }
      }
    }
    def smObjectThrow = forAll(genOverMaxDepth) { depth =>
      val smObject = SMObjectTestUtils.createNested(depth, baseKey, baseSMObject)
      Try(smObject.toObject()).toEither must throwValueLimitReached
    }
  }
}
