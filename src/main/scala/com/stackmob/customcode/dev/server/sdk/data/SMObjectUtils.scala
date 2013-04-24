package com.stackmob.customcode.dev
package server
package sdk
package data

import com.stackmob.sdkapi.{SMValue, SMObject}
import collection.JavaConverters._
import collection.mutable.{Map => MutableMap}
import SMValueUtils._
import net.liftweb.json._
import com.stackmob.customcode.sdk.SMObjectConverter

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.sdk.data
 *
 * User: aaron
 * Date: 4/15/13
 * Time: 2:05 PM
 */
object SMObjectUtils {
  implicit class SMObjectW(smObject: SMObject) {

    /**
     * convert this SMObject to a lift-json JObject. note that this method should be used directly on an SMObject,
     * rather than the more generic toJValue method, since toJValue will cause a cryptic compiler error
     * @param depth the depth that we're currently at in the recursion
     * @return the new JObject
     */
    def toJObject(depth: Int = 0): JObject = {
      if(depth > maxDepth) {
        throw DepthLimitReached(maxDepth)
      }

      val scalaMap: Map[String, SMValue[_]] = smObject.toScalaMap
      val jFields = scalaMap.map { tup =>
        val (key, smValue) = tup
        JField(key, smValue.toJValue(depth + 1))
      }
      JObject(jFields.toList)
    }

    def toScalaMap: Map[String, SMValue[_]] = {
      SMObjectConverter.getMap(smObject).asScala.toMap
    }

    def toObjectMap: Map[String, Object] = {
      toScalaMap.map { tup =>
        tup._1 -> tup._2.toObject()
      }.toMap
    }
  }
}
