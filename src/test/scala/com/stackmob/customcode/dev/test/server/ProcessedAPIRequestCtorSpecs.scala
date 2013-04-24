package com.stackmob.customcode.dev.test.server

import org.specs2.Specification

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.test.server
 *
 * User: aaron
 * Date: 4/18/13
 * Time: 6:11 PM
 */
class ProcessedAPIRequestCtorSpecs extends Specification { def is =
  "The processedAPIRequest() function".title                                                                            ^ end ^
  "The processedAPIRequest function is a convenience method for constructing a ProcessedAPIRequest"                     ^ end ^
  "The resulting ProcessedAPIRequest should match the given parameters"                                                 ! pending ^ end ^
                                                                                                                        end

}
