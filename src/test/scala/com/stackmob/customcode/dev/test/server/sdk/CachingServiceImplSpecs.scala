package com.stackmob.customcode.dev
package test
package server
package sdk

import org.specs2.Specification
import com.stackmob.customcode.dev.server.sdk.cache.CachingServiceImpl

class CachingServiceImplSpecs extends Specification { def is =
  "CachingServiceImpl".title                                                                                            ^ end ^
  "CachingService is responsible for fast storage and retrieval of key/value data"                                      ^ end ^
  "round trips should work"                                                                                             ! roundTrip ^ end ^
  end

  private val svc = new CachingServiceImpl(throwableFreq0, throwableFreq0, throwableFreq0, throwableFreq0)
  private val key = "testKey"
  private val value = "testValue".getBytesUTF8
  private val ttl = 1L
  private def roundTrip = {
    svc.setBytes(key, value, ttl)
    svc.getBytes(key) must beEqualTo(value)
  }
}
