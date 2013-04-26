package com.stackmob.customcode.dev
package server
package sdk
package data

import scalaz.Scalaz._
import com.stackmob.sdkapi._
import com.stackmob.sdk.api.StackMobDatastore
import com.stackmob.sdk.exception.{StackMobHTTPResponseException, StackMobException}
import com.stackmob.core.{InvalidSchemaException, DatastoreException}
import collection.JavaConverters._
import SMObjectUtils._
import net.liftweb.json._
import simulator.CallLimitation
import java.util.UUID

class DataServiceImpl(datastore: StackMobDatastore,
                      maxCallsPerRequest: Int = 5)
                     (implicit session: UUID) extends DataService {
  override def getUserSchema = userSchemaName

  private val allCallsLimiter = CallLimitation(maxCallsPerRequest, TooManyDataServiceCallsException(maxCallsPerRequest, _))

  private def convertSMObjectList(s: String): JavaList[SMObject] = {
    smObjectList(json.read[RawMapList](s)).asJava
  }

  private def convertSMObject(s: String): SMObject = {
    smObject(json.read[RawMap](s))
  }

  private def convert(s: StackMobException): Throwable = {
    s match {
      case e: StackMobHTTPResponseException if e.getCode == 400 => {
        new InvalidSchemaException(new String(e.getBody))
      }
      case e => {
        new DatastoreException(e.getMessage)
      }
    }
  }

  @throws(classOf[DatastoreException])
  private def getSMString(smValue: SMValue[_]): SMString = {
    smValue match {
      case str: SMString => str
      case other => throw new DatastoreException("the given SMValue must be an SMString")
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def createObject(schema: String, toCreate: SMObject): SMObject = {
    allCallsLimiter("createObject") {
      val jobj = toCreate.toJObject()
      val jsonString = compact(render(jobj))

      synchronous(datastore.post(schema, jsonString, _))
        .get
        .map(convertSMObject)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def createRelatedObjects(schema: String,
                                    objectId: SMValue[_],
                                    relatedField: String,
                                    relatedObjectsToCreate: JavaList[SMObject]): BulkResult = {
    allCallsLimiter("createRelatedObjects") {
      val objectIdString = getSMString(objectId).getValue
      val relatedObjects = relatedObjectsToCreate.asScala.map { relatedObj =>
        relatedObj.toObjectMap
      }.toList
      synchronous(datastore.postRelatedBulk(schema, objectIdString, relatedField, relatedObjects.asJava, _))
        .get
        .mapFailure(convert)
        .map { responseStr =>
          //TODO: fix this decoding
          json.read[BulkResult](responseStr)
        }
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JavaList[SMCondition]): JavaList[SMObject]= {
    allCallsLimiter("readObjects") {
      val query = smQuery(schema, conditions.asScala.toList)
      synchronous(datastore.get(query, _))
        .get
        .map(convertSMObjectList)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JavaList[SMCondition],
                           fields: JavaList[String]): JavaList[SMObject] = {
    allCallsLimiter("readObjects") {
      val query = smQuery(schema, conditions.asScala.toList, fields.asScala.toList.some)
      synchronous(datastore.get(query, _))
        .get
        .map(convertSMObjectList)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JavaList[SMCondition],
                           expandDepth: Int): JavaList[SMObject] = {
    allCallsLimiter("readObjects") {
      val query = smQuery(schema, conditions.asScala.toList)
      val options = smOptions(expandDepth)
      synchronous(datastore.get(query, options, _))
        .get
        .map(convertSMObjectList)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JavaList[SMCondition],
                           expandDepth: Int,
                           resultFilters: ResultFilters): JavaList[SMObject] = {
    allCallsLimiter("readObjects") {
      val query = smQuery(schema,
        conditions.asScala.toList,
        mbFields = resultFilters.getFields.asScala.toList.some,
        mbRange = (resultFilters.getStart -> resultFilters.getEnd).some,
        mbOrderings = resultFilters.getOrderings.asScala.toList.some)
      val options = smOptions(expandDepth, resultFilters.getFields.asScala.toList.some)

      synchronous(datastore.get(query, options, _))
        .get
        .map(convertSMObjectList)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: String,
                            updateActions: JavaList[SMUpdate]): SMObject = {
    allCallsLimiter("updateObject") {
      synchronous(datastore.put(schema, id, smBody(updateActions.asScala.toList).asJava, _))
        .get
        .map(convertSMObject)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: SMValue[_],
                            updateActions: JavaList[SMUpdate]): SMObject = {
    updateObject(schema, getSMString(id), updateActions)
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: SMValue[_],
                            conditions: JavaList[SMCondition],
                            updateActions: JavaList[SMUpdate]): SMObject = {
    //TODO: implement, needs java SDK functionality
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObjects(schema: String,
                             conditions: JavaList[SMCondition],
                             updateActions: JavaList[SMUpdate]) {
    //TODO: implement, needs java SDK functionality
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def addRelatedObjects(schema: String,
                                 objectId: SMValue[_],
                                 relation: String,
                                 relatedIds: JavaList[_ <: SMValue[_]]): SMObject = {
    addRelatedObjects(schema, objectId, relation, new SMList(relatedIds))
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def addRelatedObjects(schema: String,
                                 objectId: SMValue[_],
                                 relation: String,
                                 relatedIds: SMList[_ <: SMValue[_]]): SMObject = {
    allCallsLimiter("addRelatedObjects") {
      val relatedIdStrings = relatedIds.getValue.asScala.map { relatedIdSMValue =>
        getSMString(relatedIdSMValue)
      }
      synchronous(datastore.putRelated(schema, getSMString(objectId).getValue, relation, relatedIdStrings.toList.asJava, _))
        .get
        .map(convertSMObject)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: String): JavaBoolean = {
    allCallsLimiter("deleteObject") {
      synchronous(datastore.delete(schema, id, _))
        .get
        .map { resultStr =>
          json.read[Boolean](resultStr)
        }.mapFailure(convert).getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: SMValue[_]): JavaBoolean = {
    deleteObject(schema, getSMString(id))
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValue[_],
                                    relation: String,
                                    relatedIds: JavaList[_ <: SMValue[_]],
                                    cascadeDelete: Boolean) {
    removeRelatedObjects(schema, objectId, relation, new SMList(relatedIds), cascadeDelete)
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValue[_],
                                    relation: String,
                                    relatedIds: SMList[_ <: SMValue[_]],
                                    cascadeDelete: Boolean) {
    allCallsLimiter("removeRelatedObjects") {
      val relatedIdStrings = relatedIds.getValue.asScala.map { smValue =>
        getSMString(smValue)
      }
      synchronous(datastore.deleteIdsFrom(schema, getSMString(objectId).getValue, relation, relatedIdStrings.toList.asJava, cascadeDelete, _))
        .get
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def countObjects(schema: String): Long = {
    allCallsLimiter("countObjects") {
      synchronous(datastore.count(schema, _))
        .get
        .mapFailure(convert).map { respString =>
          json.read[Long](respString)
        }
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def getObjectModelNames: JavaSet[String] = {
    allCallsLimiter("getObjectModelNames") {
      //TODO: implement with listapi
      throw new DatastoreException("not yet implemented")
    }
  }
}
