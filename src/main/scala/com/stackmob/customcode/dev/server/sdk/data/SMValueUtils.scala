package com.stackmob.customcode.dev
package server
package sdk
package data

import com.stackmob.sdkapi._
import net.liftweb.json._
import java.math.BigInteger
import collection.JavaConverters._
import SMObjectUtils._

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.sdk.data
 *
 * User: aaron
 * Date: 4/15/13
 * Time: 2:06 PM
 */
object SMValueUtils {
  implicit class SMValueW[T](smValue: SMValue[T]) {

    private def jInt(l: Long): JInt = {
      JInt(new BigInt(new BigInteger(l.toString)))
    }

    def toJValue(depth: Int = 0): JValue = {
      if(depth > maxDepth) {
        throw DepthLimitReached(maxDepth)
      }
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
          smObj.toJObject(depth)
        }
      }
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
