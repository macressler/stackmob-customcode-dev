package com.stackmob.customcode
package localrunner
package sdk
package data

import com.stackmob.sdkapi._
import net.liftweb.json._
import java.math.BigInteger
import com.stackmob.customcode.localrunner.sdk._
import collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.data
 *
 * User: aaron
 * Date: 4/15/13
 * Time: 2:06 PM
 */
object SMValueUtils {
  implicit class SMValueW[T](smValue: SMValue[T]) {
    def underlying: T = smValue.getValue

    private def jInt(l: Long): JInt = {
      JInt(new BigInt(new BigInteger(l.toString)))
    }

    //TODO: enforce maximum expand depths

    def toJValue(depth: Int = 0): JValue = {
      smValue match {
        case smInt: SMInt => {
          val i = smInt.getValue.asInstanceOf[Long]
          jInt(i)
        }
        case smLong: SMLong => {
          val l = smLong.getValue.asInstanceOf[Long]
          jInt(l)
        }
        case smDouble: SMDouble => {
          val d = smDouble.getValue.asInstanceOf[Double]
          JDouble(d)
        }
        case smBool: SMBoolean => {
          val b = smBool.getValue.asInstanceOf[Boolean]
          JBool(b)
        }
        case smString: SMString => {
          val s = smString.getValue.asInstanceOf[String]
          JString(s)
        }
        case smList: SMList[T] => {
          val l = smList.getValue.asInstanceOf[JList[T]]
          val jValues = l.asScala.map { rawT =>
            val smValue = rawT.asInstanceOf[SMValue[T]]
            smValue.toJValue(depth + 1)
          }.toList
          JArray(jValues)
        }
        case smObj: SMObject => {
          val m = smObj.getValue.asInstanceOf[JMap[String, SMValue[_]]]
          val jFields = m.asScala.map { tup =>
            val (key, smValue) = tup
            JField(key, smValue.toJValue(depth + 1))
          }
          JObject(jFields.toList)
        }
      }
    }

    def toJsonString: String = {
      compact(render(toJValue()))
    }

    def toObject(depth: Int = 0): Object = {
      smValue match {
        case primitive: SMPrimitive[T] => primitive.getValue.asInstanceOf[Object]
        case str: SMString => str
        case list: SMList[T] => {
          val javaList = list.getValue.asInstanceOf[JList[T]]
          val objects = javaList.asScala.map { rawT =>
            val smValue = rawT.asInstanceOf[SMValue[T]]
            smValue.toObject(depth + 1)
          }
          objects.asJava
        }
        case obj: SMObject => {
          val javaMap = obj.getValue.asInstanceOf[JMap[String, SMValue[_]]]
          javaMap.asScala.map { tup =>
            val (key, smValue) = tup
            key -> smValue.toObject(depth + 1)
          }.toMap.asJava
        }
      }
    }
  }
}
