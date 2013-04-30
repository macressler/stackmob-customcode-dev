package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdkapi._
import net.liftweb.json._

trait SMValueExtensions {
  implicit class SMValueW(val smValue: SMValue[_]) {
    def fold[T](smPrimFn: SMPrimitive[_] => T,
                smStringFn: SMString => T,
                smListFn: SMList[_ <: SMValue[_]] => T,
                smObjFn: SMObject => T): T = {
      if(smValue.isA(classOf[SMPrimitive[_]])) {
        smPrimFn(smValue.asA(classOf[SMPrimitive[_]]))
      } else if(smValue.isA(classOf[SMString])) {
        smStringFn(smValue.asA(classOf[SMString]))
      } else if(smValue.isA(classOf[SMList[_ <: SMValue[_]]])) {
        smListFn(smValue.asA(classOf[SMList[_ <: SMValue[_]]]))
      } else if(smValue.isA(classOf[SMObject])) {
        smObjFn(smValue.asA(classOf[SMObject]))
      } else {
        throw new UnsupportedSMValueException(smValue)
      }
    }

    def toObject(depth: Int = 0): Object = checkDepth(depth) {
      fold(
        smPrimFn = { prim =>
          prim.toObject
        },
        smStringFn = { str =>
          str.toObject
        },
        smListFn = { lst =>
          lst.toObject(depth)
        },
        smObjFn = { obj =>
          obj.toObject(depth)
        }
      )
    }

    def toJValue(depth: Int = 0): JValue = checkDepth(depth) {
      fold(
        smPrimFn = { prim =>
          prim.toJValue
        },
        smStringFn = { str =>
          str.toJValue
        },
        smListFn = { lst =>
          lst.toJValue(depth)
        },
        smObjFn = { obj =>
          obj.toJValue(depth)
        }
      )
    }
  }
}
