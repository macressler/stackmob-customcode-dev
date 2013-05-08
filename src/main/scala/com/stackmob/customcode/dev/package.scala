package com.stackmob.customcode

import com.stackmob.sdk.api.StackMob
import com.stackmob.sdk.api.StackMob.OAuthVersion
import com.stackmob.sdk.push.StackMobPush
import scala.util.{Failure, Success, Try}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev
 *
 * User: aaron
 * Date: 4/18/13
 * Time: 4:36 PM
 */
package object dev {
  def stackMobClient(apiKey: String, apiSecret: String): StackMob = {
    new StackMob(OAuthVersion.One, 0, apiKey, apiSecret)
  }

  def stackMobPushClient(apiKey: String, apiSecret: String): StackMobPush = {
    new StackMobPush(0, apiKey, apiSecret)
  }

  type ConfigMap = Map[ConfigKey, ConfigVal]
  val DefaultConfig: ConfigMap = Map(EnableDatastoreService -> Enabled(false))
}
