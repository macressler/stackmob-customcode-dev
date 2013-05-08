package com.stackmob.customcode.dev
package server
package sdk
package data
package extensions

import com.stackmob.sdkapi.{SMValue, SMObject}
import com.stackmob.customcode.dev.server.sdk.data.SMObjectConverter
import net.liftweb.json._
import collection.JavaConverters._

trait SMObjectExtensions {
  implicit class SMObjectW(val smObject: SMObject) {
    def toScalaMap: Map[String, SMValue[_]] = {
      SMObjectConverter.getMap(smObject).asScala.toMap
    }

    def toObject(depth: Int = 0): Object = checkDepth(depth) {
      val javaMap = SMObjectConverter.getMap(smObject)
      javaMap.asScala.map { tup =>
        val (key, smValue) = tup
        key -> smValue.toObject(depth + 1)
      }.toMap.asJava
    }
    def toJValue(depth: Int = 0): JValue = {
      checkDepth(depth) {
        val scalaMap: Map[String, SMValue[_]] = smObject.toScalaMap
        val jFields = scalaMap.map { tup =>
          val (key, smValue) = tup
          val jValue = smValue.toJValue(depth + 1)
          JField(key, jValue)
        }
        JObject(jFields.toList)
      }
    }

    def toObjectMap(depth: Int = 0): Map[String, Object] = {
      checkDepth(depth) {
        toScalaMap.map { tup =>
          tup._1 -> tup._2.toObject(depth + 1)
        }.toMap
      }
    }
  }

}
