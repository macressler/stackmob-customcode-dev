package com.stackmob.customcode.dev.test.server.sdk

import org.scalacheck.Gen
import com.stackmob.customcode.dev.server._

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
}
