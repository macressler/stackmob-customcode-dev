package com.stackmob.customcode.localrunner.sdk.data

import com.stackmob.sdkapi.{SMValue, SMObject}
import net.liftweb.json._

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.data
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 5:09 PM
 */
trait SMObjectW {
  protected def smObject: SMObject

  //TODO: check for graph cycles in smObject
  def toJValue: JValue = {
    //TODO: implement this
    JNothing
  }

  def toJsonString: String = {
    compact(render(toJValue))
  }

  def toSMValueMap: Map[String, SMValue[_]] = {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  def toObjectMap: Map[String, Object] = {
    //TODO: implement this
    sys.error("not yet implemented")
  }
}
