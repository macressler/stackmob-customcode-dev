package com.stackmob.customcode.dev.test.server.sdk.data

import org.specs2.Specification
import com.stackmob.sdk.api.StackMobDatastore
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.server.json
import java.util.UUID

class DataServiceImplSpecs extends Specification with Mockito { def is =
  "DataServiceImplSpecs".title                                                                                          ^ end ^
  "DataService is the primary API for custom code to talk to the StackMob datastore"                                    ^ end ^
  "createObject should create the proper schema"                                                                        ! CreateObject().correctSchema ^ end ^
                                                                                                                        end

  private sealed trait Base {
    protected implicit lazy val session = UUID.randomUUID()
    protected def dataService(smDatastore: StackMobDatastore) = new DataServiceImpl(smDatastore)
    protected lazy val schemaName = "test-schema"
  }

  private case class CreateObject() extends Base {
    def correctSchema = {
      val map = Map("key1" -> "val1")
      val obj = smObject(map)
      val datastore = new MockStackMobDatastore(json.write(map).getBytes)
      val res = dataService(datastore).createObject(schemaName, obj)
      val called = datastore.numPostCalls.get must beEqualTo(1)
//      val returnCorrect = res must beEqualTo(obj)
//      called and returnCorrect
      called
    }
  }

}
