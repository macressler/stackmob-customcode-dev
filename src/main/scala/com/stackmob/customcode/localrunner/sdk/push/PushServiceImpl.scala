package com.stackmob.customcode
package localrunner
package sdk
package push

import com.stackmob.sdkapi.PushService
import com.stackmob.sdk.push.StackMobPush
import com.stackmob.sdkapi.PushService.{TokenType, TokenAndType}
import com.stackmob.core.{DatastoreException, PushServiceException}
import collection.JavaConverters._

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
  override def sendPushToTokens(tokens: JList[TokenAndType], pairs: JMap[String, String]) {
    val stackmobPushTokens = tokens.asScala.map { t =>
      stackmobPushToken(t)
    }.toList.asJava
    synchronous(stackmobPush.pushToTokens(pairs, stackmobPushTokens, _))
  }

  @throws(classOf[PushServiceException])
  override def sendPushToUsers(users: JList[String], pairs: JMap[String, String]) {
    synchronous(stackmobPush.pushToUsers(pairs, users, _))
  }

  @throws(classOf[PushServiceException])
  def broadcastPush(pairs: JMap[String, String]) {
    synchronous(stackmobPush.broadcastPushNotification(pairs, _))
  }

  @throws(classOf[DatastoreException])
  def getAllTokensForUsers(users: JList[String]): JMap[String, JList[TokenAndType]] = {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  override def removeToken(token: TokenAndType) {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @throws(classOf[PushServiceException])
  override def getSendableDevicesForPauload(pairs: JMap[String, String]): JSet[TokenType] = {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  override def registerTokenForUser(username: String, token: TokenAndType) {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  override def getAllExpiredTokens(clear: Boolean): JMap[TokenAndType, Long] = {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @throws(classOf[DatastoreException])
  override def getAllExpiredTokens(clear: Boolean): JMap[TokenAndType, Long] = {
    sys.error("not yet implemented")
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def sendPush(tokens: JList[String], badge: Int, sound: String, alert: String) {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def sendPush(recipients: JList[String], badge: Int, sound: String, alert: String) {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @Deprecated
  @throws(classOf[PushServiceException])
  override def broadcastPush(badge: Int, sound: String, alert: String) {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def getExpiredTokens(clear: Boolean): JMap[String, Long] = {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def registerToken(username: String, token: String) {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def getTokensForUsers(users: JList[String]): JMap[String, String] = {
    //TODO: implement this
    sys.error("not yet implemented")
  }

  @Deprecated
  @throws(classOf[DatastoreException])
  override def removeToken(token: String) {
    //TODO: implement this
    sys.error("not yet implemented")
  }

}
