package com.stackmob.customcode.localrunner.sdk.data

import com.stackmob.sdk.api.{StackMobGeoPoint, StackMobQuery}
import com.stackmob.sdkapi._
import com.stackmob.customcode.localrunner._

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.data
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 5:09 PM
 */
trait StackMobQueryW {
  protected def query: StackMobQuery

  def addSMCondition(cond: SMCondition): StackMobQuery = {
    cond match {
      case f: SMIsNull => {
        val bool: Boolean = f.getValue.underlying
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
        validating {
          query.fieldIsLessThanOrEqualTo(f.getField, f.getValue.toString.toInt)
        } | {
          query
        }
      }
      case f: SMNear => {
        val point = new StackMobGeoPoint(f.getLon.getValue, f.getLat.getValue)
        if (StackMobGeoPoint.radiansToKm(f.getDist.getValue) == 0.0) {
          query.fieldIsNear(f.getField, point)
        } else {
          val km = StackMobGeoPoint.radiansToKm(f.getDist.getValue)
          query.fieldIsNearWithinKm(f.getField,
            point,
            km)
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
      case _ => query
    }
  }
}