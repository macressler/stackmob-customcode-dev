/**
 * Copyright 2011 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.localrunner.sdk.data

import com.stackmob.core.{InvalidSchemaException, DatastoreException}
import com.stackmob.sdkapi._
import java.util.{List => JList, Map => JMap, ArrayList => JArrayList, Set => JSet}
import java.lang.{Boolean => JBoolean}
import collection.JavaConverters._
import java.net.ConnectException

class DatastoreServiceImpl(dataService: DataService) extends DatastoreService {


  private implicit def objectToSMValue(obj: Object) = anyToSMValue(obj)

  def getObjectList(list: JList[_]): JList[SMValue[_]] = list.asScala.map {
    case obj: Object => objectToSMValue(obj)
    case _ => throw new DatastoreException("unsupported type: not a java.lang.Object!")
  }.toList.asJava

  private implicit def anyToSMValue(obj: Any): SMValue[_] = obj match {
    case null => null
    case i: Boolean => new SMBoolean(i)
    case i: Double => new SMDouble(i)
    case i: Float => new SMDouble(i)
    case i: Int => new SMInt(i)
    case i: JList[_] => new SMList(getObjectList(i))
    case i: Long => new SMInt(i)
    case i: String => new SMString(i)
    case o: Object => throw new DatastoreException("unsupported type %s is not a valid object to convert to SMValue" format (o.getClass.getCanonicalName))
    case a => throw new ClassCastException("Not an SMValue, not even an object: %s" format (a.getClass.getCanonicalName))
  }

  private implicit def SMValueToObject(v: SMValue[_]): AnyRef = v match {
    case null => null
    case b: SMBoolean => b.getValue
    case d: SMDouble => d.getValue
    case i: SMInt => i.getValue
    case l: SMList[_] => getArrayList(l.getValue.asScala.map {
      case smv: SMValue[_] => SMValueToObject(smv)
      case something => throw new DatastoreException(something.toString + " is not an SMValue")
    }.toList.asJava)
    case o: SMObject => o.getValue.asScala.mapValues(SMValueToObject(_))
    case s: SMString => s.getValue
    case u => throw new DatastoreException("unsupported type %s should have been an SMValue" format (u.getClass.getCanonicalName))
  }

  private implicit def MapToSMObject(m: JMap[String, Object]): SMObject = {
    new SMObject(m.asScala.mapValues(objectToSMValue(_)).asJava)
  }

  private implicit def SMObjectToMap(obj: SMObject): JMap[String, Object] = {
    obj.getValue.asScala.mapValues(SMValueToObject(_)).asJava
  }

  @throws(classOf[InvalidSchemaException])
  @throws(classOf[DatastoreException])
  override def createObject(modelName: String, toCreate: JMap[String, Object]): JMap[String, Object] = {
    dataService.createObject(modelName, toCreate)
  }


  @throws(classOf[InvalidSchemaException])
  @throws(classOf[DatastoreException])
  override def readObjects(modelName: String,
                           queryFields: JMap[String, JList[String]]): JList[JMap[String, Object]] = {

    val conditions: JList[SMCondition] = queryFields.asScala.flatMap { tup =>
      val (field, possibleValues) = tup
      val conds = possibleValues.asScala.toList.map { possibleValue =>
        (new SMEquals(field, new SMString(possibleValue))): SMCondition
      }
      conds.toList
    }.toList.asJava

    val smObjectList: List[SMObject] = dataService.readObjects(modelName, conditions).asScala.toList
    val mapList: List[JMap[String, Object]] = smObjectList.map(SMObjectToMap(_))
    getArrayList(mapList.toList.asJava)
  }

  @throws(classOf[InvalidSchemaException])
  @throws(classOf[DatastoreException])
  override def updateObject(modelName: String,
                            objectId: String,
                            newValue: JMap[String, Object]): JMap[String, Object] = {
    val smUpdates = newValue.asScala.map({
      tup: (String, Object) =>
        val l: SMUpdate = new SMSet(tup._1, objectToSMValue(tup._2))
        l
    }).toList.asJava

    dataService.updateObject(modelName, objectId, smUpdates)
  }

  @throws(classOf[InvalidSchemaException])
  @throws(classOf[DatastoreException])
  override def deleteObject(modelName: String, objectId: String): JBoolean = {
    dataService.deleteObject(modelName, objectId)
  }

  @throws(classOf[ConnectException])
  override def getObjectModelNames: JSet[String] = {
    dataService.getObjectModelNames
  }

  private def getArrayList[T](li: => JList[T]): JArrayList[T] = {
    val result = new JArrayList[T]()
    li.asScala.foreach(result.add(_))
    result
  }


}
