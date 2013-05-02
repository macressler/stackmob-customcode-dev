package com.stackmob.customcode.dev
package server
package sdk

import com.stackmob.sdkapi._
import com.stackmob.sdk.api.{StackMobOptions, StackMobQueryField, StackMobQuery}
import collection.JavaConverters._
import data.extensions._

package object data {

//  def smOptions(expandDepth: Int,
//                mbStart: Option[Int] = None,
//                mbEnd: Option[Int] = None,
//                mbSelectedFields: Option[List[String]] = None,
//                mbOrderings: Option[List[SMOrdering]] = None): StackMobOptions = {
//    val opts = StackMobOptions.none().withDepthOf(expandDepth)
//    val opts1 = mbStart.map { start => opts.mbFilters.map { filters =>
//        opts1.withSelectedFields(filters.getFields)
//      }.getOrElse(opts1)
//      opts2
//    }
//  }

  /**
   * convert an OrderingDirection into a StackMobQuery.Ordering
   * @param direction the direction from which to convert
   * @return the resulting ordering
   */
  private def smOrdering(direction: OrderingDirection): StackMobQuery.Ordering = {
    direction match {
      case OrderingDirection.ASCENDING => StackMobQuery.Ordering.ASCENDING
      case OrderingDirection.DESCENDING => StackMobQuery.Ordering.DESCENDING
    }
  }

  /**
   * create a body (to be used with PUT requests) from a list of SMUpdates
   * @param updates the updates from which to create the body
   * @return a Map representation of the body
   */
  def smBody(updates: List[SMUpdate]): Map[String, String] = {
    updates.map { update =>
      update.tup
    }.toMap
  }

  /**
   * create a StackMobOptions from an expand depth and maybe some fields
   * @param expandDepth the expand depth
   * @param mbFields the fields from which to create the StackMobOptions
   * @return the newly created StackMobOptions
   */
  def smOptions(expandDepth: Long,
                mbFields: Option[List[String]] = None): StackMobOptions = {
    val optionsWithDepth = StackMobOptions.none().withDepthOf(expandDepth.toInt)
    mbFields.map { fields =>
      optionsWithDepth.withSelectedFields(fields.asJava)
    }.getOrElse {
      optionsWithDepth
    }
  }

  /**
   * create a StackMobQuery from a list of SMConditions
   * @param schemaName the name of the schema on which to query
   * @param conditions the conditions to apply on the query
   * @return the newly created StackMobQuery
   */
  def smQuery(schemaName: String,
              conditions: List[SMCondition],
              mbFields: Option[List[String]] = None,
              mbRange: Option[(Long, Long)] = None,
              mbOrderings: Option[List[SMOrdering]] = None): StackMobQuery = {
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

    val queryWithRange = mbRange.map { tup =>
      val (start, end) = tup
      queryWithFields.isInRange(start.toInt, end.toInt)
    }.getOrElse {
      queryWithConditions
    }

    val queryWithOrderings = mbOrderings.map { orderings =>
      orderings.foldLeft(queryWithRange) { (agg, cur) =>
        agg.fieldIsOrderedBy(cur.getField, smOrdering(cur.getDirection))

      }
    }.getOrElse {
      queryWithRange
    }

    queryWithOrderings
  }

  private def smValueMap(rawMap: Map[String, Object]): Map[String, SMValue[_]] = {
    rawMap.map { tup =>
      tup._1 -> smValue(tup._2)
    }.toMap
  }

  private def smValueList(rawList: List[Object]): List[SMValue[_]] = {
    rawList.map { elt =>
      smValue(elt)
    }
  }

  def smObject(rawMap: Map[String, Object]): SMObject = {
    new SMObject(smValueMap(rawMap).asJava)
  }

  def smObjectList(list: RawMapList): List[SMObject] = {
    list.map { rawMap =>
      smObject(rawMap)
    }
  }

  def smValue(obj: Any, depth: Int = 0): SMValue[_] = {
    if(depth > maxDepth) {
      throw SMValueDepthLimitReached(depth)
    }
    obj match {
      case b: Boolean => new SMBoolean(b)
      case l: Long => new SMInt(l)
      case d: Double => new SMDouble(d)
      case i: Int => new SMInt(i.toLong)
      case s: String => new SMString(s)
      case l: RawList => new SMList(smValueList(l).asJava)
      case m: RawMap => new SMObject(smValueMap(m).asJava)
      case t => throw NoSMValueFoundException(t)
    }
  }

  type ListWildcard = List[_]
  type RawList = List[Object]
  type RawMap = Map[String, Object]
  type RawMapList = List[RawMap]
}
