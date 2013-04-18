package com.stackmob.customcode.dev.localrunner.sdk

import com.stackmob.sdkapi.PushService.{TokenType, TokenAndType}
import com.stackmob.sdk.push.StackMobPushToken
import collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.push
 *
 * User: aaron
 * Date: 3/28/13
 * Time: 6:28 PM
 */
package object push extends JsonReaders {
  type UsersToTokensAndTypes = Map[String, TokensAndTypes]
  type TokensAndTypes = List[TokenAndType]

  val IosMaxBytes = 256
  val AndroidMaxBytes = 1024

  def tokenType(t: TokenType): StackMobPushToken.TokenType = {
    t match {
      case TokenType.iOS => StackMobPushToken.TokenType.iOS
      case TokenType.Android => StackMobPushToken.TokenType.Android
      case TokenType.AndroidGCM => StackMobPushToken.TokenType.AndroidC2DM
    }
  }

  def tokenAndType(token: String, tokenType: TokenType): TokenAndType = {
    new TokenAndType(token, tokenType)
  }

  def stackmobPushToken(t: TokenAndType): StackMobPushToken = {
    new StackMobPushToken(t.getToken, tokenType(t.getType))
  }

  def iosMap(badge: Int, sound: String, alert: String): JMap[String, String] = {
    JMap("badge" -> badge.toString, "sound" -> sound, "alert" -> alert)
  }

  def iosList(list: JList[String]): JList[TokenAndType] = {
    list.asScala.map { token =>
      tokenAndType(token, TokenType.iOS)
    }.toList.asJava
  }
}
