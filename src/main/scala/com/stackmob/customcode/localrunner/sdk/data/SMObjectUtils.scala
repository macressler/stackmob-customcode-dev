package com.stackmob.customcode.localrunner.sdk.data

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
    //TODO: check for graph cycles in smObject
    def toJValue(depth: Int = 0): JValue = {
      sys.error("not yet implemented")
    }

    def toObjectMap: Map[String, Object] = {
      smObject.getValue.asScala.map { tup =>
        val (key, smValue) = tup
        key -> smValue.toObject
      }
    }
  }


}
