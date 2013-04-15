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

package com.stackmob.customcode.localrunner
package sdk
package data

import com.stackmob.core.{InvalidSchemaException, DatastoreException}
import com.stackmob.sdkapi._
import java.net.ConnectException
import collection.JavaConverters._
import SMValueUtils._
import SMObjectUtils._

class DatastoreServiceImpl(dataService: DataService) extends DatastoreService {


  @throws(classOf[InvalidSchemaException])
  @throws(classOf[DatastoreException])
  override def createObject(modelName: String, toCreate: JMap[String, Object]): JMap[String, Object] = {
    val smObjectToCreate = smObject(toCreate.asScala.toMap)
    dataService.createObject(modelName, smObjectToCreate).toObjectMap().asJava
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
    val mapList: List[JMap[String, Object]] = smObjectList.map { smObject =>
      smObject.toObjectMap().asJava
    }
    getArrayList(mapList.toList.asJava)
  }

  @throws(classOf[InvalidSchemaException])
  @throws(classOf[DatastoreException])
  override def updateObject(modelName: String,
                            objectId: String,
                            newValue: JMap[String, Object]): JMap[String, Object] = {
    val smUpdates = newValue.asScala.map({
      tup: (String, Object) =>
        val l: SMUpdate = new SMSet(tup._1, smValue(tup._2))
        l
    }).toList.asJava

    dataService.updateObject(modelName, objectId, smUpdates).toObjectMap().asJava
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
