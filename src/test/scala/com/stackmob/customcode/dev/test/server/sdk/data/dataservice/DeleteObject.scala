package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import org.specs2.mock.Mockito

private[dataservice] trait DeleteObject extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  protected case class DeleteObject() extends BaseTestContext

}
