package com.stackmob.customcode.localrunner
package sdk

import com.stackmob.sdkapi._
import com.stackmob.sdk.api.{StackMobOptions, StackMobQueryField, StackMobQuery}
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

  implicit def smUpdateToW(u: SMUpdate): SMUpdateW = new SMUpdateW {
    override protected lazy val update = u
  }

//  def smOptions(expandDepth: Int,
//                mbStart: Option[Int] = None,
//                mbEnd: Option[Int] = None,
//                mbSelectedFields: Option[List[String]] = None,
//                mbOrderings: Option[List[SMOrdering]] = None): StackMobOptions = {
//    val opts = StackMobOptions.none().withDepthOf(expandDepth)
//    val opts1 = mbStart.map { start => opts.mbFilters.map { filters =>
//      opts1.withSelectedFields(filters.getFields)
//    }.getOrElse(opts1)
//    opts2
//  }

  private def smOrdering(direction: OrderingDirection): StackMobQuery.Ordering = {
    direction match {
      case OrderingDirection.ASCENDING => StackMobQuery.Ordering.ASCENDING
      case OrderingDirection.DESCENDING => StackMobQuery.Ordering.DESCENDING
    }
  }



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
    //TODO: check for recursive definitions and excessive depth
      tup._1 -> smValue(tup._2)
    }.toMap
  }

  private def smValueList(rawList: List[Object]): List[SMValue[_]] = {
    rawList.map { elt =>
      //TODO: check for recursive definitions and excessive depth
      smValue(elt)
    }
  }

  def smObject(rawMap: Map[String, Object]): SMObject = {
    new SMObject(smValueMap(rawMap).asJava)
  }

  def smObjectList(list: List[Map[String, Object]]): List[SMObject] = {
    list.map { rawMap =>
      smObject(rawMap)
    }
  }

  def smValue(obj: Any): SMValue[_] = {
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
