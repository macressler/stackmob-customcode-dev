package com.stackmob.customcode.localrunner.sdk

import com.stackmob.sdkapi.PushService.{TokenType, TokenAndType}
import com.stackmob.sdk.push.StackMobPushToken

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.push
 *
 * User: aaron
 * Date: 3/28/13
 * Time: 6:28 PM
 */
package object push {
  def tokenType(t: TokenType): StackMobPushToken.TokenType = {
    t match {
      case TokenType.iOS => StackMobPushToken.TokenType.iOS
      case TokenType.Android => StackMobPushToken.TokenType.Android
      case TokenType.AndroidGCM => StackMobPushToken.TokenType.AndroidC2DM
    }
  }

  def stackmobPushToken(t: TokenAndType): StackMobPushToken = {
    new StackMobPushToken(t.getToken, tokenType(t.getType))
  }
}
