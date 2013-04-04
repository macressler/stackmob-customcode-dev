package com.stackmob.customcode.localrunner
package sdk
package data

import scalaz._
import scalaz.Scalaz._
import scalaz.concurrent._
import com.stackmob.sdkapi._
import com.stackmob.sdk.api.{StackMobOptions, StackMobDatastore}
import com.stackmob.sdk.callback.StackMobCallback
import com.stackmob.sdk.exception.{StackMobHTTPResponseException, StackMobException}
import com.stackmob.core.{InvalidSchemaException, DatastoreException}
import java.util.concurrent.LinkedBlockingQueue
import collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.data
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 5:03 PM
 */
class DataServiceImpl(datastore: StackMobDatastore) extends DataService {
  override def getUserSchema = userSchemaName


  private def convertSMObjectList(s: String): JList[SMObject] = {
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
    synchronous(datastore.post(schema, toCreate.toJsonString, _))
      .get
      .map(convertSMObject)
      .mapFailure(convert)
      .getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def createRelatedObjects(schema: String,
                                    objectId: SMValue[_],
                                    relatedField: String,
                                    relatedObjectsToCreate: JList[SMObject]): BulkResult = {
    val objectIdString = getSMString(objectId).underlying
    val relatedObjects = relatedObjectsToCreate.asScala.map { relatedObj =>
      relatedObj.toObjectMap
    }.toList
    synchronous(datastore.postRelatedBulk(schema, objectIdString, relatedField, relatedObjects.asJava, _))
      .get
      .mapFailure(convert)
      .map { responseStr =>
        //TODO: fix this
        json.read[BulkResult](responseStr)
      }
      .getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition]): JList[SMObject]= {
    val query = smQuery(schema, conditions.asScala.toList)
    synchronous(datastore.get(query, _))
      .get
      .map(convertSMObjectList)
      .mapFailure(convert)
      .getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           fields: JList[String]): JList[SMObject] = {
    val query = smQuery(schema, conditions.asScala.toList, fields.asScala.toList.some)
    synchronous(datastore.get(query, _))
      .get
      .map(convertSMObjectList)
      .mapFailure(convert)
      .getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           expandDepth: Int): JList[SMObject] = {
    val query = smQuery(schema, conditions.asScala.toList)
    val options = smOptions(expandDepth)
    synchronous(datastore.get(query, options, _))
      .get
      .map(convertSMObjectList)
      .mapFailure(convert)
      .getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           expandDepth: Int,
                           resultFilters: ResultFilters): JList[SMObject] = {
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

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: String,
                            updateActions: JList[SMUpdate]): SMObject = {
    synchronous(datastore.put(schema, id, smBody(updateActions.asScala.toList).asJava, _))
      .get
      .map(convertSMObject)
      .mapFailure(convert)
      .getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: SMValue[_],
                            updateActions: JList[SMUpdate]): SMObject = {
    updateObject(schema, getSMString(id), updateActions)
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: SMValue[_],
                            conditions: JList[SMCondition],
                            updateActions: JList[SMUpdate]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObjects(schema: String,
                             conditions: JList[SMCondition],
                             updateActions: JList[SMUpdate]) {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def addRelatedObjects(schema: String,
                                 objectId: SMValue[_],
                                 relation: String,
                                 relatedIds: JList[_ <: SMValue[_]]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def addRelatedObjects(schema: String,
                                 objectId: SMValue[_],
                                 relation: String,
                                 relatedIds: SMList[_ <: SMValue[_]]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: String): JBoolean = {
    synchronous(datastore.delete(schema, id, _))
      .get
      .map { resultStr =>
        json.read[Boolean](resultStr)
      }.mapFailure(convert).getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: SMValue[_]): JBoolean = {
    deleteObject(schema, getSMString(id))
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValue[_],
                                    relation: String,
                                    relatedIds: JList[_ <: SMValue[_]],
                                    cascadeDelete: Boolean) {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValue[_],
                                    relation: String,
                                    relatedIds: SMList[_ <: SMValue[_]],
                                    cascadeDelete: Boolean) {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def countObjects(schema: String): Long = {
    synchronous(datastore.count(schema, _))
      .get
      .mapFailure(convert).map { respString =>
        json.read[Long](respString)
      }
      .getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def getObjectModelNames: JSet[String] = {
    //TODO: implement with listapi
    throw new DatastoreException("not yet implemented")
  }
}
