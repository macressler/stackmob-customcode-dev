package com.stackmob.customcode.dev
package test
package localrunner

import org.specs2.Specification
import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi.SDKServiceProvider
import collection.JavaConverters._
import concurrent.duration._
import org.specs2.mock.Mockito
import concurrent.TimeoutException
import com.stackmob.newman.test.DummyHttpClient

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 4:21 PM
 */
class APIRequestProxySpecs extends Specification with Mockito { def is =
  "APIRequestProxySpecs".title                                                                                          ^ end ^
  "APIRequestProxy is responsible for executing non-custom code requests to the Stackmob API"                           ^ end ^
  "the proxy should fail if given an unknown verb"                                                                      ! pending ^ end ^
                                                                                                                        end
}
