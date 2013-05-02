package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.test.CustomMatchers
import com.stackmob.sdkapi.SMString
import collection.JavaConverters._

class DataServiceImplSpecs
  extends Specification
  with Mockito
  with CustomMatchers
  with CreateObject
  with CreateRelatedObjects
  with ReadObjects { def is =
  "DataServiceImplSpecs".title                                                                                          ^ end ^
  "DataService is the primary API for custom code to talk to the StackMob datastore"                                    ^ end ^
  "createObject should"                                                                                                 ^
    "create the proper schema"                                                                                          ! CreateObject().correctSchema ^
    "convert the response to an SMObject correctly"                                                                     ! CreateObject().correctResponse ^
    "handle common errors properly"                                                                                     ! CreateObject().commonErrors { (svc, schemaName, obj) =>
      svc.createObject(schemaName, obj)
    } ^
  end ^
  "createRelatedObjects should"                                                                                         ^
    "throw if the given objectId isn't an SMString"                                                                     ! CreateRelatedObjects().throwNotSMString ^
    "decode the result properly"                                                                                        ! CreateRelatedObjects().decodeResult ^
    "handle common errors properly"                                                                                     ! CreateRelatedObjects().commonErrors { (svc, schema, obj) =>
      svc.createRelatedObjects(schema, new SMString("hello world"), "relatedfield", List(obj).asJava)
    } ^
  end ^
  "readObjects should"                                                                                                  ^
    "read all objects correctly"                                                                                        ! ReadObjects().readsAllCorrectly ^
    "read all objects given conditions"                                                                                 ! pending ^
    "read only the requested fields"                                                                                    ! pending ^
    "read to the given expand depth properly"                                                                           ! pending ^
    "throw when the max expand depth is reached"                                                                        ! pending ^
    "honor all result filters properly"                                                                                 ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "updateObject should"                                                                                                 ^
    "work on the proper schema and ID"                                                                                  ! pending ^
    "apply the right update actions"                                                                                    ! pending ^
    "only work when conditions are met"                                                                                 ! pending ^
    "throw if an SMString wasn't given"                                                                                 ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "updateObjects should"                                                                                                ^
    "work on the right schema"                                                                                          ! pending ^
    "only work when conditions are met"                                                                                 ! pending ^
    "apply the right update actions"                                                                                    ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "addRelatedObjects should"                                                                                            ^
    "work on the correct parent schema"                                                                                 ! pending ^
    "throw if an SMString wasn't given for the object ID"                                                               ! pending ^
    "throw if any of the related IDs aren't SMStrings"                                                                  ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "deleteObject should"                                                                                                 ^
    "operate on the correct schema"                                                                                     ! pending ^
    "throw if an SMString wasn't given for the object ID"                                                               ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "removeRelatedObjects should"                                                                                         ^
    "operate on the correct parent schema"                                                                              ! pending ^
    "throw if the given object id isn't an SMString"                                                                    ! pending ^
    "throw if any of the given related IDs aren't SMStrings"                                                            ! pending ^
    "honor the cascadeDelete flag"                                                                                      ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "countObjects should"                                                                                                 ^
    "operate on the correct schema"                                                                                     ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "getObjectModelNames should"                                                                                          ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end
}
