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

import java.lang.{Boolean => JBoolean}
import JavaConversions._
import collection.JavaConversions._
import collection.immutable.{Map => SMap, List => SList}
import com.stackmob.sdkapi.DatastoreService
import java.util.{Map => JMap, List => JList, Set => JSet}

abstract class DatastoreServiceScalaAdapter extends DatastoreService {

  protected def createObjectInternal(modelName:String, toCreate:SMap[String, Object]):SMap[String, Object]
  protected def readObjectsInternal(modelName:String, queryFields:SMap[String, SList[String]]):SList[SMap[String, Object]]
  protected def updateObjectInternal(modelName:String, objectId:String, newValue:SMap[String, Object]):SMap[String, Object]
  protected def deleteObjectInternal(modelName:String, objectId:String):Boolean
  protected def getObjectModelNamesInternal:Set[String]

  override def createObject(modelName: String, toCreate: JMap[String, Object]): JMap[String, Object] = {
    val scalaMap = mapAsScalaMap(toCreate).toMap
    createObjectInternal(modelName, scalaMap)
  }

  override def readObjects(modelName:String, queryFields:JMap[String, JList[String]]):JList[JMap[String, Object]] = {
    val ret = readObjectsInternal(modelName, queryFields)
    ret
  }

  override def updateObject(modelName:String, objectId:String, newValue:JMap[String, Object]):JMap[String, Object] = {
    val scalaMap = mapAsScalaMap(newValue).toMap
    updateObjectInternal(modelName, objectId, scalaMap)
  }

  override def deleteObject(modelName:String, objectId:String):JBoolean = deleteObjectInternal(modelName, objectId)

  override def getObjectModelNames:JSet[String] = setAsJavaSet(getObjectModelNamesInternal)
}