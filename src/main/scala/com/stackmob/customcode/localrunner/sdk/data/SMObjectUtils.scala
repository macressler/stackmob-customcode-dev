package com.stackmob.customcode
package localrunner
package sdk
package data

import com.stackmob.sdkapi.SMObject
import net.liftweb.json._
import collection.JavaConverters._
import SMValueUtils._

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.data
 *
 * User: aaron
 * Date: 4/15/13
 * Time: 2:05 PM
 */
object SMObjectUtils {

  implicit class SMObjectW(smObject: SMObject) {
    def toObjectMap: Map[String, Object] = {
      val scalaMap = smObject.getValue.asScala
      scalaMap.map { tup =>
        val (key, smValue) = tup
        val obj = smValue.toObject()
        key -> obj
      }.toMap
    }
  }


}
