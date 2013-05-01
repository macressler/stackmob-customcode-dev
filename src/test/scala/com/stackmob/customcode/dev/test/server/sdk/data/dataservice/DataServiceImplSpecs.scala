package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import com.stackmob.sdk.api.StackMobDatastore
import org.specs2.mock.Mockito
import com.stackmob.customcode.dev.server.sdk.data._
import com.stackmob.customcode.dev.server.json
import java.util.UUID
import com.stackmob.customcode.dev.server.sdk.simulator.CallLimitation
import scala.util.Try
import DataServiceImpl._
import com.stackmob.sdkapi.SMObject
import com.stackmob.core.{DatastoreException, InvalidSchemaException}
import org.specs2.specification.Fragments
import com.stackmob.customcode.dev.test.CustomMatchers

class DataServiceImplSpecs
  extends Specification
  with Mockito
  with CustomMatchers
  with CreateObjectFragments { def is =
  "DataServiceImplSpecs".title                                                                                          ^ end ^
  "DataService is the primary API for custom code to talk to the StackMob datastore"                                    ^ end ^
  createObjectFragments ^
  "createRelatedObjects should"                                                                                         ^
    "throw if the given objectId isn't an SMString"                                                                     ! pending ^
    "decode the result properly"                                                                                        ! pending ^
    "handle common errors properly"                                                                                     ! pending ^
                                                                                                                        end ^
  "readObjects should"                                                                                                  ^
    "read all objects correctly"                                                                                        ! pending ^
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
