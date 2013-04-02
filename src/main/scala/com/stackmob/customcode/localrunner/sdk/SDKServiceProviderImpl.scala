/**
 * Copyright 2011 StackMob
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

package com.stackmob.customcode.localrunner.sdk

import cache.CachingServiceImpl
import com.stackmob.sdkapi._
import caching.CachingService
import com.stackmob.customcode.localrunner.sdk.data._
import configvar.ConfigVarServiceImpl
import facebook.FacebookServiceImpl
import http.HttpServiceImpl
import logger.LoggerServiceImpl
import com.stackmob.sdk.api.StackMob
import com.stackmob.sdkapi.http.HttpService
import push.PushServiceImpl
import com.stackmob.sdk.push.StackMobPush
import twitter.TwitterServiceImpl

class SDKServiceProviderImpl(stackmob: StackMob, stackmobPush: StackMobPush) extends SDKServiceProvider {
  override lazy val getDatastoreService: DatastoreService = new DatastoreServiceImpl(getDataService)
  override lazy val getDataService: DataService = new DataServiceImpl(stackmob.getDatastore)
  override lazy val getPushService: PushService = new PushServiceImpl(stackmobPush)
  override lazy val getTwitterService: TwitterService = new TwitterServiceImpl
  override lazy val getFacebookService: FacebookService = new FacebookServiceImpl
  override lazy val isSandbox: Boolean = true
  override lazy val getVersion: String = "scalaCCExample"
  override lazy val getConfigVarService: ConfigVarService = new ConfigVarServiceImpl
  override lazy val getCachingService: CachingService = new CachingServiceImpl
  override lazy val getHttpService: HttpService = new HttpServiceImpl
  override def getLoggerService(s: String) = new LoggerServiceImpl(s)
  override def getLoggerService(c: Class[_]) = getLoggerService(c.getCanonicalName)
}