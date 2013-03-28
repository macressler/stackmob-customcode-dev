package com.stackmob.customcode.localrunner
package sdk.data

import scalaz._
import scalaz.Scalaz._
import scalaz.concurrent._
import com.stackmob.sdkapi._
import com.stackmob.sdk.api.StackMobDatastore
import com.stackmob.sdk.callback.StackMobCallback
import com.stackmob.sdk.exception.{StackMobHTTPResponseException, StackMobException}
import com.stackmob.core.{InvalidSchemaException, DatastoreException}
import java.util.concurrent.LinkedBlockingQueue
import java.util.{List => JList, Set => JSet}

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.data
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 5:03 PM
 */
class DataServiceMockImpl(datastore: StackMobDatastore) extends DataService {
  override def getUserSchema = userSchemaName

  type Ex = Validation[StackMobException, String]

  private def synchronous(fn: StackMobCallback => Unit): Promise[Ex] = {
    val q = new LinkedBlockingQueue[Ex](1)
    val callback = new StackMobCallback {
      def failure(e: StackMobException) {
        q.put(e.fail)
      }
      def success(responseBody: String) {
        q.put(responseBody.success)
      }
    }
    Promise(fn(callback))
    Promise(q.take)
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
  @throws(classOf[InvalidSchemaException])
  override def createObject(schema: String, toCreate: SMObject): SMObject = {
    val postPromise = synchronous(datastore.post(schema, toCreate.toJsonString, _))
    postPromise.get.map { resultStr =>
      smObject(resultStr)
    } ||| { t: StackMobException =>
      throw convert(t)
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def createRelatedObjects(schema: String,
                                    objectId: SMValue,
                                    relatedField: String,
                                    relatedObjectsToCreate: JList[SMObject]): BulkResult = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition]): List[SMObject]= {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           fields: JList[String]): JList[SMObject] = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def readObjects(schema: String,
                           conditions: JList[SMCondition],
                           expandDepth: Int): JList[SMObject] = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
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
                            id: SMValue,
                            updateActions: JList[SMUpdate]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def updateObject(schema: String,
                            id: SMValue,
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
                                 objectId: SMValue,
                                 relation: String,
                                 relatedIds: JList[_ <: SMValue]): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def addRelatedObjects(schema: String,
                                 objectId: SMValue,
                                 relation: String,
                                 relatedIds: SMList): SMObject = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: String): Boolean = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObject(schema: String,
                            id: SMValue): Boolean = {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValue,
                                    relation: String,
                                    relatedIds: JList[_ <: SMValue], cascadeDelete: Boolean) {
    //TODO: implement
    throw new DatastoreException("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def removeRelatedObjects(schema: String,
                                    objectId: SMValue,
                                    relation: String,
                                    relatedIds: SMList, cascadeDelete: Boolean) {
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
