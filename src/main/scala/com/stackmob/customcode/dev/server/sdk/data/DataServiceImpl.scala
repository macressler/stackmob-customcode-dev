package com.stackmob.customcode.dev
package server
package sdk
package data

import scalaz.Scalaz._
import com.stackmob.sdkapi._
import com.stackmob.sdk.api._
import com.stackmob.sdk.exception.{StackMobHTTPResponseException, StackMobException}
import com.stackmob.core.{InvalidSchemaException, DatastoreException}
import collection.JavaConverters._
import extensions._
import net.liftweb.json._
import simulator.CallLimitation
import java.util.UUID
import DataServiceImpl._
import net.liftweb.json.scalaz.JsonScalaz._
import scala.util.Try

class DataServiceImpl(stackMob: StackMob,
                      maxCallsPerRequest: Int = DefaultMaxCallsPerRequest,
                      allCallsLimiter: CallLimitation = DefaultCallLimitation)
                     (implicit session: UUID) extends DataService {
  override def getUserSchema = userSchemaName

  private lazy val datastore = {
    val s = stackMob.getDatastore
    s
  }

  private def convertSMObjectList(s: String): JavaList[SMObject] = {
    val tried = for {
      jValue <- s.toJValue
      read <- Try {
        jValue.toResult[List[Map[String, Any]]].map { lst =>
          lst.map { elt =>
            elt.toMapStringObj
          }
        } | {
          throw new DatastoreException(s"invalid response $s")
        }
      }
      objList <- Try {
        smObjectList(read)
      }
    } yield {
      objList.asJava
    }
    tried.get
  }

  private def convertSMObject(s: String): SMObject = {
    val read = json.read[Map[String, String]](s)
    smObject(read)
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
      val jObject = toCreate.toJValue()
      val jsonString = compact(render(jObject))

      synchronous(datastore.post(schema, jsonString, _))
        .get
        .map(convertSMObject)
        .mapFailure(convert)
        .getOrThrow
    }
  }

  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def createObjects(schema: String,
                             toCreate: JavaList[SMObject]): BulkResult = {
    synchronous(datastore.postBulk(schema, toCreate, _)).get.mapFailure(convert).map { respStr =>
      val resp = json.read[PostBulkResponse](respStr)
      val successSMValues = resp.succeeded.map { idStr => smValue(idStr) }
      val failSMValues = resp.failed.map { idStr => smValue(idStr) }
      new BulkResult(successSMValues.asJava, failSMValues.asJava)
    }.getOrThrow
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
        relatedObj.toObjectMap()
      }.toList
      synchronous(datastore.postRelatedBulk(schema, objectIdString, relatedField, relatedObjects.asJava, _)).get.mapFailure { f =>
        convert(f)
      }.map { responseStr =>
        val resp = json.read[PostRelatedResponse](responseStr)
        val successSMValues = resp.succeeded.map { idStr =>
          smValue(idStr)
        }
        val failSMValues = resp.failed.map { idStr =>
          smValue(idStr)
        }
        new BulkResult(successSMValues.asJava, failSMValues.asJava)
      }.getOrThrow
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
      val query = smQuery(schema, conditions.asScala.toList)
      val options = smOptions(1, fields.asScala.toList.some)
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
      synchronous(datastore.put(schema, id, json.write(smBody(updateActions.asScala.toList)), _))
        .get
        .map { resString =>
          convertSMObject(resString)
        }
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
    deleteObject(schema, getSMString(id).getValue)
  }

  /*
  Implementation note: the StackMob REST API supports delete-by-query, and in
  turn the StackMob Java Client SDK is able to use this feature. However,
  the REST API only returns whether a delete-by-query was successful; it does
  not return how many objects were deleted as a result of the operation.

  As a result, the emulated Custom Code method here must make three outgoing
  calls: a count of objects matching a query before delete, the delete itself,
  and a count of objects matching the query after delete. Ideally, the
  post-delete count would be zero, but it is possible by means of data
  replication and propagation lag for the post-delete count to be nonzero.
  Additionally, since these method calls are not handled transactionally,
  any objects created between calls may disrupt the final count.

  As such, please be aware that the results of this method may not be 100%
  accurate to its effects, as a limitation of its implementation outside of
  the real Custom Code environment.

  Note that this inaccuracy is *not* the case within the real Custom Code
  environment; a delete is a single call to the datastore, which returns a
  value immediately, and that value is accurate to the number deleted.
   */
  @throws(classOf[DatastoreException])
  @throws(classOf[InvalidSchemaException])
  override def deleteObjects(schema: String,
                             conditions: java.util.List[SMCondition]): Long = {
    allCallsLimiter("deleteObjects") {
      val query = smQuery(schema, conditions.asScala.toList)
      val preCount = synchronous(datastore.count(query, _))
        .get
        .mapFailure(convert)
        .map { respString =>
          json.read[Long](respString)
        }
        .getOrThrow
      synchronous(datastore.delete(query, _))
        .get
        .mapFailure(convert)
        .getOrThrow
      val postCount = synchronous(datastore.count(query, _))
        .get
        .mapFailure(convert)
        .map { respString =>
          json.read[Long](respString)
        }
        .getOrThrow
      preCount - postCount
    }
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
        .mapFailure(convert)
        .map { respString =>
          json.read[Long](respString)
        }
        .getOrThrow
    }
  }

  def countObjects(schema: String,
                   conditions: JavaList[SMCondition]): Long = {
    allCallsLimiter("countObjects") {
      val query = smQuery(schema, conditions.asScala.toList)
      synchronous(datastore.count(query, _))
        .get
        .mapFailure(convert)
        .map { respString =>
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

object DataServiceImpl {
  val DefaultMaxCallsPerRequest = 5
  //since each CallLimitation contains state, we should create a new one each time it's requested
  def DefaultCallLimitation = {
    CallLimitation(DefaultMaxCallsPerRequest) { seq =>
      TooManyDataServiceCallsException(DefaultMaxCallsPerRequest, seq)
    }
  }

  //TODO: actually throw this when a call limit is reached
  class CallsPerRequestLimitExceeded(maxCalls: Int)
    extends Exception(s"you tried to make more than $maxCalls datastore calls in a single custom code request")

  case class PostBulkResponse(succeeded: List[String], failed: List[String])
  case class PostRelatedResponse(succeeded: List[String], failed: List[String])

  implicit def listJSONR[T: JSONR]: JSONR[List[T]] = new JSONR[List[T]] {
    override def read(json: JValue): Result[List[T]] = {
      json match {
        case JArray(arr) => {
          val listOfResults: List[Result[T]] = arr.map { jValue =>
            fromJSON[T](jValue)
          }
          listOfResults.sequence[Result, T]
        }
        case other => UnexpectedJSONError(other, classOf[JArray]).failNel[List[T]]
      }
    }
  }

  implicit val mapJSONR: JSONR[Map[String, Any]] = new JSONR[Map[String, Any]] {
    override def read(json: JValue): Result[Map[String, Any]] = {
      json match {
        case JObject(fields) => {
          fields.map { field =>
            field.values: (String, Any)
          }.toMap.successNel[Error]
        }
        case other => {
          UnexpectedJSONError(other, classOf[JObject]).failNel[Map[String, Any]]
        }
      }
    }
  }

  implicit val listMapJSONR: JSONR[List[Map[String, Any]]] = listJSONR(mapJSONR)

  implicit class MapStringAnyW(map: Map[String, Any]) {
    def toMapStringObj: Map[String, Object] = {
      map.map { tup =>
        //TODO: see if there's a better way to do this without the asInstanceOf
        tup._1 -> tup._2.asInstanceOf[Object]
      }
    }
  }
}
