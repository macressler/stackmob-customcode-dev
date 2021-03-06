/**
 * Copyright 2011-2013 StackMob
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

package com.stackmob.customcode.dev.test.server.sdk.data

import collection.JavaConversions._
import com.stackmob.sdkapi.DatastoreService
import org.specs2.Specification
import com.stackmob.customcode.dev.server.sdk.data.{DataServiceImpl, DatastoreServiceImpl}
import com.stackmob.sdk.api.{StackMob, StackMobDatastore}
import collection.JavaConverters._
import org.mockito.Mockito.mock
import java.util.UUID
import org.specs2.mock.Mockito

class DatastoreServiceImplSpecs extends Specification with Mockito{ def is =
  "DatastoreService".title                                                                                              ^ end ^
  """DatastoreService is a (deprecated) custom code interface to access the StackMob datastore"""                       ^ end ^
  "crud should work properly"                                                                                           ! pending ^ end ^ //crud() ^ end ^
                                                                                                                        end
  implicit private val uuid = UUID.randomUUID()

  private val dataService = new DataServiceImpl(mock[StackMob])
  private val datastoreService = new DatastoreServiceImpl(dataService)
  private val modelName = "testModel"
  private val data = Map("hello" -> "world", "hello1" -> "world1")
  private val query = Map("hello" -> seqAsJavaList(List("world")))

  private val invalidQueries = List(
    Map("hello" -> seqAsJavaList(List("world1"))),
    Map("asdasfafasd" -> seqAsJavaList(List("world")))
  )

  private val pkKey = "objectId"

  private def mapEquals(m1:Map[String, Object], m2:Map[String, Object], exceptKey:String = pkKey) = {
    val newM1 = m1 - exceptKey
    val newM2 = m2 - exceptKey

    newM1 must haveTheSameElementsAs(newM2)
  }

  private def crud() = {
    val created: Map[String, Object] = datastoreService.createObject(modelName, data).asScala.toMap
    val createdEq = mapEquals(data, created)
    val createdHasPK = created.get(pkKey) must beSome

    createdEq and createdHasPK

//    val pk = created(pkKey).toString
//
//    val read = ds.readObjects(modelName, query).asScala
//    val readSizeRes = read.size must beEqualTo(1)
//    val readMapRes = read.get(0) must beSome.like {
//      case m => {
//        mapEquals(m, data)
//      }
//    }
//
//    assertEquals(1, read.size)
//    assertMapEquals(read(0), data)
//
//    val newData = data ++ Map("hello3" -> "world3")
//    val updated = ds.updateObject(modelName, pk, newData)
//    assertMapEquals(newData, updated)
//
//    val readAfterUpdate:List[Map[String, Object]] =
//      ds.readObjects(modelName, Map("hello3" -> seqAsJavaList(List("world3"))))
//
//    assertEquals(1, readAfterUpdate.size)
//    val dataAfterUpdate = readAfterUpdate(0)
//    assertTrue(dataAfterUpdate.contains(pkKey))
//    val pkAfterUpdate = dataAfterUpdate(pkKey).toString
//    assertTrue(pk.equals(pkAfterUpdate))
//
//    assertMapEquals(newData, dataAfterUpdate)
//    assertTrue(pk.equals(pkAfterUpdate))
//
//    val deletedRes = ds.deleteObject(modelName, dataAfterUpdate(pkKey).toString) must beTrue
//
//    val afterDeleteRes = ds.readObjects(modelName, query).size() must beEqualTo(0)
  }

//  @Test
//  def deleteAll() {
//    val numToCreate = 2
//    val createdIds = for(i <- 0 until numToCreate)
//      yield ds.createObject(modelName, data).get(pkKey).toString;
//
//    assertEquals(numToCreate, createdIds.size)
//
//    def deleteAndVerify(ids:IndexedSeq[String]) {
//      val deletionNum = numToCreate - ids.size + 1
//      try {
//        val objectId = ids.head
//        assertTrue("couldn't delete object " + deletionNum + " with objectID " + objectId, ds.deleteObject(modelName, objectId))
//        deleteAndVerify(ids.tail)
//      }
//      catch {
//        case e => //do nothing
//      }
//    }
//
//    deleteAndVerify(createdIds)
//
//    val readAfterDeleted:List[Map[String, Object]] = ds.readObjects(modelName, query)
//    assertEquals(0, readAfterDeleted.size)
//  }
//
//  @Test
//  def queryNothing() {
//    val created:Map[String, Object] = ds.createObject(modelName, data)
//    val createdId = created(pkKey).toString
//
//    import java.util.{List => JList}
//    def assertInvalidQuery(query:Map[String, JList[String]]) {
//      val read:List[Map[String, Object]] = ds.readObjects(modelName, query)
//      assertEquals(0, read.size)
//    }
//
//    for(q <- invalidQueries) assertInvalidQuery(q)
//
//    ds.deleteObject(modelName, createdId)
//  }
}