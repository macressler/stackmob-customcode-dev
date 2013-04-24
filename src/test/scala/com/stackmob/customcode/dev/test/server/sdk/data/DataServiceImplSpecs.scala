package com.stackmob.customcode.dev.test.server.sdk.data

import org.specs2.Specification
import com.stackmob.sdk.api.StackMobDatastore
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.sdk.callback.StackMobRawCallback
import java.util.UUID

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data
 *
 * User: aaron
 * Date: 4/24/13
 * Time: 1:57 PM
 */
class DataServiceImplSpecs extends Specification with Mockito { def is =
  "DataServiceImplSpecs".title                                                                                          ^ end ^
  "DataService is the primary API for custom code to talk to the StackMob datastore"                                    ^ end ^
  "createObject should create the proper schema"                                                                        ! CreateObject().correctSchema ^ end ^
                                                                                                                        end
  private def smDatastore = mock[StackMobDatastore]
  private def dataService = {
    implicit val session = UUID.randomUUID()
    new DataServiceImpl(smDatastore)
  }
  private lazy val schemaName = "test-schema"
  private case class CreateObject() {
    def correctSchema = {
      val obj = smObject(Map("obj1" -> "obj1Value"))
      val res = dataService.createObject(schemaName, obj)
      val called = there was one(smDatastore).post(schemaName, any[String], any[StackMobRawCallback])
      val returnCorrect = res must beEqualTo(obj)
      called and returnCorrect
    }
  }

}
