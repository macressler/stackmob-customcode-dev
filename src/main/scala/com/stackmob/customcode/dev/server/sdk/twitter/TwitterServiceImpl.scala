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
package twitter

import com.stackmob.sdkapi.TwitterService
import com.stackmob.core.TwitterServiceException
import org.slf4j.LoggerFactory
import java.util.UUID

class TwitterServiceImpl(implicit session: UUID) extends TwitterService {
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
