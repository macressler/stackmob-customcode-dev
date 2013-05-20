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

package com.stackmob.customcode.dev
package server
package sdk
package push

import com.stackmob.sdkapi.PushService
import com.stackmob.sdk.push.StackMobPush
import com.stackmob.sdkapi.PushService.{TokenType, TokenAndType => CCTokenAndType}
import com.stackmob.core.{DatastoreException, PushServiceException}
import collection.JavaConverters._
import net.liftweb.json.{JValue}
import net.liftweb.json.scalaz.JsonScalaz._

class PushServiceImpl(stackmobPush: StackMobPush) extends PushService {

  @throws(classOf[PushServiceException])
  override def sendPushToTokens(tokens: JavaList[CCTokenAndType], pairs: JavaMap[String, String]) {
    val stackmobPushTokens = tokens.asScala.map { t =>
      stackmobPushToken(t)
    }.toList.asJava
    synchronous(stackmobPush.pushToTokens(pairs, stackmobPushTokens, _)).get.getOrThrow
  }

  @throws(classOf[PushServiceException])
  override def sendPushToUsers(users: JavaList[String], pairs: JavaMap[String, String]) {
    synchronous(stackmobPush.pushToUsers(pairs, users, _))
  }

  @throws(classOf[PushServiceException])
  def broadcastPush(pairs: JavaMap[String, String]) {
    synchronous(stackmobPush.broadcastPushNotification(pairs, _)).map { validation =>
      validation.mapFailure { t =>
        new PushServiceException(t.getMessage)
      }
    }.get.getOrThrow
  }

  private implicit val tokensMapJSONR: JSONR[UsersToTokensAndTypes] = new JSONR[UsersToTokensAndTypes] {
    override def read(json: JValue): Result[UsersToTokensAndTypes] = {
      field[UsersToTokensAndTypes]("tokens")(json)
    }
  }

  @throws(classOf[DatastoreException])
  def getAllTokensForUsers(users: JavaList[String]): JavaMap[String, JavaList[CCTokenAndType]] = {
    val validation = for {
      respString <- synchronous(stackmobPush.getTokensForUsers(users, _)).get.mapFailure { t =>
        new DatastoreException(t.getMessage)
      }
      respJValue <- respString.toJValue.toValidation.mapFailure { t =>
        new DatastoreException(t.getMessage)
      }
      respMap <- respJValue.toResult[UsersToTokensAndTypes].mapFailure { failNel =>
        new DatastoreException("result string from StackMob was malformed\n%s".format(respString))
      }
    } yield respMap

    validation.getOrThrow.map { tup =>
      tup._1 -> tup._2.asJava
    }.asJava
  }

  @throws(classOf[DatastoreException])
  override def removeToken(token: CCTokenAndType) {
    synchronous(stackmobPush.removePushToken(stackmobPushToken(token), _)).get.mapFailure { t =>
      new DatastoreException(t.getMessage)
    }.getOrThrow
  }

  @throws(classOf[PushServiceException])
  override def getSendableDevicesForPayload(pairs: JavaMap[String, String]): JavaSet[TokenType] = {
    val serialized = json.write(pairs)
    val numBytes = serialized.getBytesUTF8.length
    Set(
      (if(numBytes > IosMaxBytes) None else Some(TokenType.iOS)),
      (if(numBytes > AndroidMaxBytes) None else Some(TokenType.Android))
    ).flatMap { mbTokenType =>
      mbTokenType.map { tokenType =>
        Set(tokenType)
      }.getOrElse(Set[TokenType]())
    }.asJava
  }

  @throws(classOf[DatastoreException])
  override def registerTokenForUser(username: String, token: CCTokenAndType) {
    synchronous(stackmobPush.registerForPushWithUser(stackmobPushToken(token), username, _)).get.mapFailure { t =>
      new DatastoreException(t.getMessage)
    }.getOrThrow
  }

  @throws(classOf[DatastoreException])
  override def getAllExpiredTokens(clear: Boolean): JavaMap[CCTokenAndType, JavaLong] = {
    //TODO: endpoint for this in push API
    throw new DatastoreException("not yet implemented")
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def sendPush(recipients: JavaList[String], badge: Int, sound: String, alert: String) {
    sendPushToTokens(iosList(recipients), iosMap(badge, sound, alert))
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def sendPush(recipients: JavaList[String], badge: Int, sound: String, alert: String, recipientsAreTokens: Boolean) {
    if(recipientsAreTokens) {
      sendPushToTokens(iosList(recipients), iosMap(badge, sound, alert))
    } else {
      sendPushToUsers(recipients, iosMap(badge, sound, alert))
    }
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def broadcastPush(badge: Int, sound: String, alert: String) {
    broadcastPush(JavaMap("badge" -> badge.toString, "sound" -> sound, "alert" -> alert))
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def getExpiredTokens(clear: Boolean): JavaMap[String, JavaLong] = {
    //TODO: endpoint for this in push API
    throw new DatastoreException("not yet implemented")
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def registerToken(username: String, token: String) {
    registerTokenForUser(username, tokenAndType(token, TokenType.iOS))
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def getTokensForUsers(users: JavaList[String]): JavaMap[String, String] = {
    val map = getAllTokensForUsers(users).asScala.flatMap { tup =>
      val (user, tokensAndTypes) = tup
      tokensAndTypes.asScala.map { tokenAndType =>
        user -> tokenAndType.getToken
      }.toList
    }

    map.asJava
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def removeToken(token: String) {
    removeToken(tokenAndType(token, TokenType.iOS))
  }

}
