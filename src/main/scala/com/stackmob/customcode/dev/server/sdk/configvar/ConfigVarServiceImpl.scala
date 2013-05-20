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

package com.stackmob.customcode.dev.server.sdk.configvar

import com.stackmob.sdkapi.ConfigVarService
import com.stackmob.core.ConfigVarServiceException

class ConfigVarServiceImpl extends ConfigVarService {


  //TODO: keep the app ID so we can calculate the app hash to use to get real config vars
  //(https://github.com/stackmob/lucid/blob/master/provisioning.md#get)

  @throws(classOf[ConfigVarServiceException])
  override def get(key: String): String = {
    "test-config-var-%s".format(key)
  }

  @throws(classOf[ConfigVarServiceException])
  override def get(key: String, moduleName: String): String = {
    "test-module-config-var-%s-%s".format(key, moduleName)
  }
}
