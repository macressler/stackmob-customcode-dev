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

import com.stackmob.sdkapi._
import caching.CachingService
import data.{DataServiceImpl, DatastoreServiceImpl, DatastoreServiceMockImpl}
import http.HttpService
import org.mockito.Mockito._
import com.stackmob.sdk.api.{StackMobDatastore, StackMob}

class SDKServiceProviderMockImpl(datastore: StackMobDatastore) extends SDKServiceProvider {
  override lazy val getDatastoreService: DatastoreService = new DatastoreServiceImpl(getDataService)
  override lazy val getDataService: DataService = new DataServiceImpl(datastore)
  override lazy val getPushService: PushService = mock(classOf[PushService])
  override lazy val getTwitterService: TwitterService = mock(classOf[TwitterService])
  override lazy val getFacebookService: FacebookService = mock(classOf[FacebookService])
  override lazy val isSandbox: Boolean = true
  override lazy val getVersion: String = "scalaCCExample"
  override lazy val getConfigVarService: ConfigVarService = mock(classOf[ConfigVarService])
  override lazy val getCachingService: CachingService = mock(classOf[CachingService])
  override lazy val getHttpService: HttpService = mock(classOf[HttpService])

  override def getLoggerService(s: String) = mock(classOf[LoggerService])
  override def getLoggerService(c: Class[_]) = getLoggerService(c.getCanonicalName)
}