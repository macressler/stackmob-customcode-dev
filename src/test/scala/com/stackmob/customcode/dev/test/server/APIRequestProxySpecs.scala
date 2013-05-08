package com.stackmob.customcode.dev
package test
package server

import org.specs2.Specification
import org.specs2.mock.Mockito

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server
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
