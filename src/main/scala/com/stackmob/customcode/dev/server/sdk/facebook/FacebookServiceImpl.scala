package com.stackmob.customcode.dev
package server
package sdk
package facebook

import com.stackmob.sdkapi.FacebookService
import org.slf4j.LoggerFactory
import com.stackmob.core.FacebookServiceException
import java.util.UUID

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.server.sdk.facebook
 * 
 * User: aaron
 * Date: 4/2/13
 * Time: 3:46 PM
 */
class FacebookServiceImpl(implicit session: UUID) extends FacebookService {
  private lazy val logger = LoggerFactory.getLogger(classOf[FacebookService])

  @throws(classOf[FacebookServiceException])
  override def createUserWithFacebookId(modelName: String, username: String, accessToken: String): Boolean = {
    logger.info("createUserWithFacebookId. modelName = %s, username = %s, accessToken = %s".format(modelName, username, accessToken))
    true
  }

  @throws(classOf[FacebookServiceException])
  override def linkFacebookIdToUser(modelName: String, username: String, accessToken: String): Boolean = {
    logger.info("linkFacebookIdToUser. modelName = %s, username = %s, accessToken = %s".format(modelName, username, accessToken))
    true
  }

  @throws(classOf[FacebookServiceException])
  override def unlinkFacebookIdFromUser(modelName: String, username: String) {
    logger.info("unlinkFacebookIdFromUser. modelName = %s, username = %s".format(modelName, username))
  }

  @throws(classOf[FacebookServiceException])
  override def findUser(modelName: String, accessToken: String): String = {
    logger.info("findUser. modelName = %s, accessToken = %s".format(modelName, accessToken))
    testFBUser
  }

  @throws(classOf[FacebookServiceException])
  override def publishMessage(modelName: String, username: String, messageText: String): String = {
    logger.info("publishMessage. modelName = %s, username = %s, messageText = %s".format(modelName, username, messageText))
    testFBMessageID
  }
}
