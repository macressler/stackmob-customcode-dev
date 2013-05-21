package com.stackmob.customcode.dev.test.server

import org.specs2.Specification

class ProcessedAPIRequestCtorSpecs extends Specification { def is =
  "The processedAPIRequest() function".title                                                                            ^ end ^
  "The processedAPIRequest function is a convenience method for constructing a ProcessedAPIRequest"                     ^ end ^
  "The resulting ProcessedAPIRequest should match the given parameters"                                                 ! pending ^ end ^
                                                                                                                        end

}
