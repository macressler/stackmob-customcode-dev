package com.stackmob.customcode
package localrunner
package sdk
package twitter

import com.stackmob.sdkapi.TwitterService
import com.stackmob.core.TwitterServiceException
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.twitter
 * 
 * User: aaron
 * Date: 4/2/13
 * Time: 3:58 PM
 */
class TwitterServiceImpl extends TwitterService {
  private lazy val logger = LoggerFactory.getLogger(classOf[TwitterService])

  @throws(classOf[TwitterServiceException])
  override def createUserWithTwitter(modelName: String, smUsername: String, token: String, tokenSecret: String): Boolean = {
    logger.info("createUserWithTwitter. modelName = %s, smUsername = %s, token = %s, tokenSecret = %s".format(modelName, smUsername, token, tokenSecret))
    true
  }

  @throws(classOf[TwitterServiceException])
  override def linkUserWithTwitter(modelName: String, smUsername: String, token: String, tokenSecret: String): Boolean = {
    logger.info("linkUserWithTwitter. modelName = %s, smUsername = %s, token = %s, tokenSecret = %s".format(modelName, smUsername, token, tokenSecret))
    true
  }

  @throws(classOf[TwitterServiceException])
  override def unlinkUserFromTwitter(modelName: String, username: String) {
    logger.info("unlinkUserFromTwitter. modelName = %s, username = %s".format(modelName, username))
  }

  @throws(classOf[TwitterServiceException])
  override def findAndVerifyUser(modelName: String, token: String, tokenSecret: String): String = {
    logger.info("findAndVerifyUser. modelName = %s, token = %s, tokenSecret = %s".format(modelName, token, tokenSecret))
    testTwitterUser
  }

  @throws(classOf[TwitterServiceException])
  override def findAndVerifyUser(modelName: String, twUserId: String): String = {
    logger.info("findAndVerifyUser. modelName = %s, twUserId = %s".format(modelName, twUserId))
    userName
  }

  @throws(classOf[TwitterServiceException])
  override def findUsername(modelName: String, twUserId: String): String = {
    logger.info("findUsername. modelName = %s, twUserId = %s".format(modelName, twUserId))
    userName
  }

  @throws(classOf[TwitterServiceException])
  override def updateStatus(modelName: String, smUsername: String, statusMsg: String): Boolean = {
    logger.info("updateStatus. modelName = %s, smUsername = %s, statusMsg = %s".format(modelName, smUsername, statusMsg))
    true
  }

  @throws(classOf[TwitterServiceException])
  override def verifyCredentials(modelName: String, smUsername: String): Boolean = {
    logger.info("verifyCredentials. modelName = %s, smUsername = %s".format(modelName, smUsername))
    true
  }
}
