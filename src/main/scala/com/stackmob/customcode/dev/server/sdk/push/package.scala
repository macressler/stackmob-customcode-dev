/**
 * Copyright 2011-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.dev.server.sdk

import com.stackmob.sdkapi.PushService.{TokenType, TokenAndType}
import com.stackmob.sdk.push.StackMobPushToken
import collection.JavaConverters._

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

  def iosMap(badge: Int, sound: String, alert: String): JavaMap[String, String] = {
    JavaMap("badge" -> badge.toString, "sound" -> sound, "alert" -> alert)
  }

  def iosList(list: JavaList[String]): JavaList[TokenAndType] = {
    list.asScala.map { token =>
      tokenAndType(token, TokenType.iOS)
    }.toList.asJava
  }
}
