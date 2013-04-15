package com.stackmob.customcode
package localrunner
package sdk
package data

import com.stackmob.sdkapi.{SMValue, SMObject}
import collection.JavaConverters._
import collection.mutable.{Map => MutableMap}
import SMValueUtils._
import net.liftweb.json._

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

    def toScalaMap = {
      val keys = smObject.getValue.keySet().asScala
      val mutableMap = MutableMap[String, SMValue[_]]()

      /**
       * we have to build the map manually intead of calling toMap on a sequence of tuples
       * because we don't have a <pre><:<</pre> available for A <:< (String, SMValue[_])
       */
      keys.map { key =>

      /**
       * the asInstanceOf call is necessary because the scala compiler thinks that the result of the get
       * call is an SMValue instead of an SMValue[_]. the result of SMObject being defined as
       * <pre>Map<String, SMValue></pre> instead of <pre>Map<String, SMValue<?>></pre>
       */
        val smValue = smObject.getValue.get(key).asInstanceOf[SMValue[_]]
        mutableMap += (key -> smValue)
      }
      mutableMap.toMap
    }

    def toObjectMap: Map[String, Object] = {
      toScalaMap.map { tup =>
        tup._1 -> tup._2.toObject()
      }.toMap
    }
  }
}
