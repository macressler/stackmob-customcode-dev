package com.stackmob.customcode.dev.server.sdk.data.extensions

import com.stackmob.sdk.api.{StackMobGeoPoint, StackMobQuery}
import com.stackmob.sdkapi._
import com.stackmob.customcode.dev.server._
import collection.JavaConverters._

trait StackMobQueryExtensions {
  implicit class StackMobQueryW(query: StackMobQuery) {

    def addSMCondition(cond: SMCondition, depth: Int = 0): StackMobQuery = {
      if(depth >= maxDepth) {
        throw SMConditionDepthLimitReached(maxDepth)
      }
      cond match {
        case f: SMIsNull => {
          val bool: Boolean = f.getValue.getValue
          if (bool) {
            query.fieldIsNull(f.getField)
          }
          else {
            query.fieldIsNotNull(f.getField)
          }
        }
        case f: SMIn => {
          val smValues = f.getValues
          val inValues = smValues.asScala.map { smValue =>
            smValue.toString
          }
          query.fieldIsIn(f.getField, inValues.toList.asJava)
        }
        case f: SMEquals => {
          query.fieldIsEqualTo(f.getField, f.getValue.toString)
        }
        case f: SMNotEqual => {
          query.fieldIsNotEqual(f.getField, f.getValue.toString)
        }
        case f: SMGreater => {
          query.fieldIsGreaterThan(f.getField, f.getValue.toString)
        }
        case f: SMGreaterOrEqual => {
          query.fieldIsGreaterThanOrEqualTo(f.getField, f.getValue.toString)
        }
        case f: SMLess => {
          query.fieldIsLessThan(f.getField, f.getValue.toString)
        }
        case f: SMLessOrEqual => {
          query.fieldIslessThanOrEqualTo(f.getField, f.getValue.toString)
        }
        case f: SMNear => {
          val point = new StackMobGeoPoint(f.getLon.getValue, f.getLat.getValue)
          val km = StackMobGeoPoint.radiansToKm(f.getDist.getValue)
          if (km == 0.0) {
            query.fieldIsNear(f.getField, point)
          } else {
            query.fieldIsNearWithinKm(f.getField, point, km)
          }
        }
        case f: SMWithin => {
          val point = new StackMobGeoPoint(f.getLon.getValue, f.getLat.getValue)
          val km = StackMobGeoPoint.radiansToKm(f.getDist.getValue)
          query.fieldIsWithinRadiusInKm(f.getField, point, km)
        }
        case f: SMWithinBox => {
          val lowerLeft = new StackMobGeoPoint(f.getLonLL.getValue, f.getLatLL.getValue)
          val upperRight = new StackMobGeoPoint(f.getLonUR.getValue, f.getLatUR.getValue)
          query.fieldIsWithinBox(f.getField, lowerLeft, upperRight)
        }
        case f: SMOr => {
          throw new UnsupportedOperationException("SMOr is not currently supported")
        }
        case f: SMAnd => {
          val clauses = f.getClauses.asScala
          clauses.foldLeft(query) { (curQuery, clause) =>
            curQuery.addSMCondition(clause, depth + 1)
          }
        }
        case _ => query
      }
    }
  }
}
