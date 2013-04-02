package com.stackmob.customcode.localrunner
package sdk

import com.stackmob.sdkapi._
import com.stackmob.sdk.api.{StackMobQueryField, StackMobQuery}

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

  /**
   * create a StackMobQuery from a list of SMConditions
   * @param schemaName the name of the schema on which to query
   * @param conditions the conditions to apply on the query
   * @return the newly created StackMobQuery
   */
  def smQuery(schemaName: String,
              conditions: List[SMCondition],
              mbFields: Option[List[String]] = None): StackMobQuery = {
    val q = new StackMobQuery(schemaName)
    val queryWithConditions = conditions.foldLeft(q) { (agg, cur) =>
      agg.addSMCondition(cur)
    }

    val queryWithFields = mbFields.map { fields =>
      fields.foldLeft(queryWithConditions) { (agg, cur) =>
        agg.field(new StackMobQueryField(cur))
      }
    }.getOrElse {
      queryWithConditions
    }

    queryWithFields
  }

  //TODO: implement this
  def smObject(map: Map[String, Object]): SMObject = {
    sys.error("not yet implemented")
  }

  //TODO: implement this
  def smObjectList(list: List[Map[String, Object]]): List[SMObject] = {
    sys.error("not yet implemented")
  }

  //TODO: implement this
  def smValue(a: Any): SMValueWildcard = {
    sys.error("not yet implemented")
  }

  type SMValueWildcard = SMValue[_]
  type RawMap = Map[String, Object]
  type RawMapList = List[RawMap]
}
