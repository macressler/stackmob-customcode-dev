package com.stackmob.customcode.dev
package test
package server
package sdk

import org.scalacheck.Gen
import com.stackmob.customcode.dev.server._
import com.stackmob.sdk.api._
import java.util.concurrent.Executors
import com.stackmob.sdk.api.StackMob.OAuthVersion
import com.stackmob.sdk.callback._
import java.util

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server.sdk.data
 *
 * User: aaron
 * Date: 4/25/13
 * Time: 4:55 PM
 */
package object data {
  //don't use large numbers here because it may affect runtime of construction of nested structures
  val genOverMaxDepth = Gen.choose(maxDepth + 1, maxDepth + 10)

  private[data] val datastoreExecutorService = Executors.newCachedThreadPool()
  private[data] val datastoreSession = new StackMobSession(OAuthVersion.One, 0, "test-key", "test-secret", "test-user", "test-userid")

  private[data] val datastoreRedirectedCallback = new StackMobRedirectedCallback {
    override def redirected(originalUrl: String, redirectHeaders: util.Map[String, String], redirectBody: String, newURL: String) {}
  }
}
