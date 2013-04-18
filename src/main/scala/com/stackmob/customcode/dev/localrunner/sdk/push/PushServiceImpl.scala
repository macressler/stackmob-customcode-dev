package com.stackmob.customcode.dev
package localrunner
package sdk
package push

import com.stackmob.sdkapi.PushService
import com.stackmob.sdk.push.StackMobPush
import com.stackmob.sdkapi.PushService.{TokenType, TokenAndType => CCTokenAndType}
import com.stackmob.core.{DatastoreException, PushServiceException}
import collection.JavaConverters._
import net.liftweb.json.{parse, JValue}
import net.liftweb.json.scalaz.JsonScalaz._

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.push
 * 
 * User: aaron
 * Date: 3/28/13
 * Time: 6:11 PM
 */
class PushServiceImpl(stackmobPush: StackMobPush) extends PushService {

  @throws(classOf[PushServiceException])
  override def sendPushToTokens(tokens: JList[CCTokenAndType], pairs: JMap[String, String]) {
    val stackmobPushTokens = tokens.asScala.map { t =>
      stackmobPushToken(t)
    }.toList.asJava
    synchronous(stackmobPush.pushToTokens(pairs, stackmobPushTokens, _)).get.getOrThrow
  }

  @throws(classOf[PushServiceException])
  override def sendPushToUsers(users: JList[String], pairs: JMap[String, String]) {
    synchronous(stackmobPush.pushToUsers(pairs, users, _))
  }

  @throws(classOf[PushServiceException])
  def broadcastPush(pairs: JMap[String, String]) {
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
  def getAllTokensForUsers(users: JList[String]): JMap[String, JList[CCTokenAndType]] = {
    val validation = for {
      respString <- synchronous(stackmobPush.getTokensForUsers(users, _)).get.mapFailure { t =>
        new DatastoreException(t.getMessage)
      }
      respJValue <- validating(parse(respString)).mapFailure { t =>
        new DatastoreException(t.getMessage)
      }
      respMap <- fromJSON[UsersToTokensAndTypes](respJValue).mapFailure { failNel =>
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
  override def getSendableDevicesForPayload(pairs: JMap[String, String]): JSet[TokenType] = {
    val serialized = json.write(pairs)
    val numBytes = serialized.getBytes.length
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
  override def getAllExpiredTokens(clear: Boolean): JMap[CCTokenAndType, JLong] = {
    //TODO: endpoint for this in push API
    JMap[CCTokenAndType, JLong]()
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def sendPush(recipients: JList[String], badge: Int, sound: String, alert: String) {
    sendPushToTokens(iosList(recipients), iosMap(badge, sound, alert))
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def sendPush(recipients: JList[String], badge: Int, sound: String, alert: String, recipientsAreTokens: Boolean) {
    if(recipientsAreTokens) {
      sendPushToTokens(iosList(recipients), iosMap(badge, sound, alert))
    } else {
      sendPushToUsers(recipients, iosMap(badge, sound, alert))
    }
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def broadcastPush(badge: Int, sound: String, alert: String) {
    broadcastPush(JMap("badge" -> badge.toString, "sound" -> sound, "alert" -> alert))
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def getExpiredTokens(clear: Boolean): JMap[String, JLong] = {
    //TODO: endpoint for this in push API
    JMap[String, JLong]()
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def registerToken(username: String, token: String) {
    registerTokenForUser(username, tokenAndType(token, TokenType.iOS))
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def getTokensForUsers(users: JList[String]): JMap[String, String] = {
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
