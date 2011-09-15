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

import com.stackmob.core.DatastoreException
import java.util.UUID

class DatastoreServiceMockImpl(appName:String, initialModels:List[String]) extends DatastoreServiceScalaAdapter {

  private var db = Map[String, List[Map[String, Object]]]()
  for(m <- initialModels) {
    db = db + ((m, List[Map[String, Object]]()))
  }

  private val pkFieldName = "objectId"

  private def ensureModelExists(modelName:String):List[Map[String, Object]] = {
    db.get(modelName) match {
      case Some(lst) => lst
      case None => throw new DatastoreException("no such model " + modelName)
    }
  }

  private def setObjectId(data:Map[String, Object], pk:String) = data + ((pkFieldName, pk))
  private def setObjectId(data:Map[String, Object]) = data + ((pkFieldName, getObjectId))

  private def getObjectId = UUID.randomUUID.toString

  private def findByObjectId(modelName:String, objectId:String) = {
    val lst = ensureModelExists(modelName)
    lst.find((elt:Map[String, Object]) => elt(pkFieldName).equals(objectId)) match {
      case None => throw new DatastoreException("no document with objectId " + objectId + " in model " + modelName)
      case Some(elt: Map[String, Object]) => elt
    }
  }

  override protected def createObjectInternal(modelName:String, toCreate:Map[String, Object]) = {
    val toCreateWithObjectId = setObjectId(toCreate)

    db.get(modelName) match {
      case None => db = db + ((modelName, List(toCreateWithObjectId)))
      case Some(lst:List[Map[String, Object]]) => {
        val newList = lst ++ List(toCreateWithObjectId)
        db = db - modelName + ((modelName, newList))
      }
    }

    toCreateWithObjectId
  }

  override protected def readObjectsInternal(modelName:String, queryFields:Map[String, List[String]]):List[Map[String, Object]] = {
    val list = ensureModelExists(modelName)
    list.filter((elt:Map[String, Object]) => {
      queryFields.filter((kv:(String, List[String])) => {
        val key = kv._1
        val possibleVals = kv._2
        elt.get(key) match {
          case None => false
          case Some(obj:Object) => possibleVals.find((pVal:String) => pVal == obj) match {
            case None => false
            case _ => true
          }
        }
      }).size == queryFields.size
    })
  }

  override protected def updateObjectInternal(modelName:String, objectId:String, newValue:Map[String, Object]) = {
    newValue.get(pkFieldName) match {
      case None =>
      case Some(elt) => throw new DatastoreException(pkFieldName + " not allowed in values to update")
    }

    val obj = findByObjectId(modelName, objectId)
    val oldObjectId = obj(pkFieldName).toString
    val oldCollection = db(modelName)
    val oldCollectionRemoved = oldCollection.filterNot((doc:Map[String, Object]) => doc == obj)

    val newValueWithObjectId = setObjectId(newValue, oldObjectId)
    val newValueList:List[Map[String, Object]] = List(newValueWithObjectId.toMap)
    val collection:List[Map[String, Object]] = oldCollectionRemoved ++ newValueList
    db = db.filterKeys((k:String) => !k.equals(modelName)) + ((modelName, collection))
    newValue
  }

  override protected def deleteObjectInternal(modelName:String, objectId:String):Boolean = {
    val list = ensureModelExists(modelName)
    val newList = list.filter((elt:Map[String, Object]) => !objectId.equals(elt(pkFieldName)))
    db = db - modelName + ((modelName, newList))
    (newList.size == (list.size - 1))
  }

  override protected def getObjectModelNamesInternal = db.keySet
}