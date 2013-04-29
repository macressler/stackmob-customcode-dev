package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi.{SMValue, SMList}
import com.stackmob.customcode.dev.server._
import net.liftweb.json._
import collection.JavaConverters._

trait SMListExtensions {
  implicit class SMListW(val smList: SMList[_ <: SMValue[_]]) {
    def toObject(depth: Int = 0): Object = checkDepth(maxDepth) {
      val javaList = smList.getValue
      val objects = javaList.asScala.map { rawT =>
        rawT.toObject(depth + 1)
      }
      objects.asJava
    }

    def toJValue(depth: Int = 0): JValue = checkDepth(maxDepth) {
      val jValues = smList.getValue.asScala.map { rawT =>
        val smValue = rawT.asInstanceOf[SMValue[_]]
        smValue.toJValue(depth + 1)
      }.toList
      JArray(jValues)
    }
  }
}
