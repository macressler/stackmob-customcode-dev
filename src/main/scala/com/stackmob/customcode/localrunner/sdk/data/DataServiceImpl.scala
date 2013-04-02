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
  @throws(classOf[InvalidSchemaException])
  override def createObject(schema: String, toCreate: SMObject): SMObject = {
    synchronous(datastore.post(schema, toCreate.toJsonString, _)).get.map { resultStr =>
      smObject(json.read[RawMap](resultStr))
    }.mapFailure(convert).getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def createRelatedObjects(schema: String,
                                    objectId: SMValueWildcard,
                                    relatedField: String,
                                    relatedObjectsToCreate: JList[SMObject]): BulkResult = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition]): JList[SMObject]= {
    val query = smQuery(schema, conditions.asScala.toList)
    synchronous(datastore.get(query, _)).get.map { resultStr =>
      smObjectList(json.read[RawMapList](resultStr)).asJava
    }.mapFailure(convert).getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           fields: JList[String]): JList[SMObject] = {
    val query = smQuery(schema, conditions.asScala.toList, fields.asScala.toList.some)
    synchronous(datastore.get(query, _)).get.map { resultStr =>
      smObjectList(json.read[RawMapList](resultStr)).asJava
    }.mapFailure { t =>
      convert(t)
    }.getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           expandDepth: Int): JList[SMObject] = {
    val query = smQuery(schema, conditions.asScala.toList)
    val options = new StackMobOptions().withDepthOf(expandDepth)
    synchronous(datastore.get(query, options, _)).get.map { resultStr =>
      smObjectList(json.read[RawMapList](resultStr)).asJava
    }.mapFailure(convert).getOrThrow
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           expandDepth: Int,
                           resultFilters: ResultFilters): JList[SMObject] = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: String,
                            updateActions: JList[SMUpdate]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: SMValueWildcard,
                            updateActions: JList[SMUpdate]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: SMValueWildcard,
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
                                 objectId: SMValueWildcard,
                                 relation: String,
                                 relatedIds: JList[_ <: SMValueWildcard]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def addRelatedObjects(schema: String,
                                 objectId: SMValueWildcard,
                                 relation: String,
                                 relatedIds: SMList[_ <: SMValueWildcard]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: String): JBoolean = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: SMValueWildcard): JBoolean = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValueWildcard,
                                    relation: String,
                                    relatedIds: JList[_ <: SMValueWildcard],
                                    cascadeDelete: Boolean) {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValueWildcard,
                                    relation: String,
                                    relatedIds: SMList[_ <: SMValueWildcard],
                                    cascadeDelete: Boolean) {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def countObjects(schema: String): Long = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def getObjectModelNames: JSet[String] = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }
}
