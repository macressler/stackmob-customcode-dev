package com.stackmob.customcode.localrunner
package sdk

import com.stackmob.sdkapi._
import com.stackmob.sdk.api.{StackMobGeoPoint, StackMobQuery}
import collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.data
 *
 * User: aaron
 * Date: 3/27/13
 * Time: 4:16 PM
 */
package object data {
  implicit def smValueToW[T](s: SMValue[T]): SMValueW[T] = new SMValueW[T] {
    override protected lazy val smValue = s
  }

  implicit def stackMobQueryToW(q: StackMobQuery): StackMobQueryW = new StackMobQueryW {
    override protected lazy val query = q
  }

  implicit def smObjectToW(s: SMObject): SMObjectW = new SMObjectW {
    override protected lazy val smObject = s
  }

  def smQuery(objectName: String, conditions: List[SMCondition]): StackMobQuery = {
    val q = new StackMobQuery()
    q.setObjectName(objectName)
    val newQuery = conditions.foldLeft(q) { (agg, cur) =>
      agg.addSMCondition(cur)
    }
    newQuery
  }

  //TODO: implement this
  def smObject(s: String): SMObject = {
    val map = Map[String, SMValue[_]]()
    new SMObject(map.asJava)
  }

}
