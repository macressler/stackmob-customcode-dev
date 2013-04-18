package com.stackmob.customcode.dev.localrunner.sdk.configvar

import com.stackmob.sdkapi.ConfigVarService
import com.stackmob.core.ConfigVarServiceException

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.configvar
 * 
 * User: aaron
 * Date: 4/2/13
 * Time: 3:05 PM
 */
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
