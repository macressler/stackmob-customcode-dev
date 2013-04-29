package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi._
import net.liftweb.json._

trait SMValueExtensions {
  implicit class SMValueW(val smValue: SMValue[_]) {
    def fold[T](smPrimFn: SMPrimitive[_] => T,
                smStringFn: SMString => T,
                smListFn: SMList[_] => T,
                smObjFn: SMObject => T): T = {
      if(smValue.isA(classOf[SMPrimitive[_]])) {
        smPrimFn(smValue.asA(classOf[SMPrimitive[_]]))
      } else if(smValue.isA(classOf[SMString])) {
        smStringFn(smValue.asA(classOf[SMString]))
      } else if(smValue.isA(classOf[SMList[_]])) {
        smListFn(smValue.asA(classOf[SMList[_]]))
      } else {
        smObjFn(smValue.asA(classOf[SMObject]))
      }
    }

    def toObject(depth: Int = 0): Object = checkDepth(depth) {
      fold(_.toObject(depth), _.toObject(depth), _.toObject(depth), _.toObject(depth))
    }

    def toJValue(depth: Int = 0): JValue = checkDepth(depth) {
      fold(_.toJValue(depth), _.toJValue(depth), _.toJValue(depth), _.toJValue(depth))
    }
  }
}
