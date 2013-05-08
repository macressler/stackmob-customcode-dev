package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import org.specs2.mock.Mockito

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data.dataservice
 *
 * User: aaron
 * Date: 5/7/13
 * Time: 4:51 PM
 */
private[dataservice] trait RemoveRelated extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  protected case class RemoveRelated() extends BaseTestContext

}
