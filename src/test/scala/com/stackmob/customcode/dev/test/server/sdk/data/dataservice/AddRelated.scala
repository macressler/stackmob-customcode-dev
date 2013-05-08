package com.stackmob.customcode.dev
package test
package server
package sdk
package data
package dataservice

import org.specs2.Specification
import org.specs2.mock.Mockito

private[dataservice] trait AddRelated extends BaseTestGroup { this: Specification with CustomMatchers with Mockito =>
  protected case class AddRelated() extends BaseTestContext
}
