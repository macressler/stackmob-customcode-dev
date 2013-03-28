package com.stackmob.customcode.localrunner.sdk.cache

import com.stackmob.sdkapi.caching.CachingService
import com.google.common.cache.{Cache, CacheBuilder}
import java.util.concurrent.TimeUnit

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.cache
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 6:37 PM
 */
class CachingServiceImpl extends CachingService {
  private val maxSize = 1000
  private type Value = (String, Long)
  private val cache = CacheBuilder
    .newBuilder()
    .maximumSize(maxSize)
    //expire every entry 1 second after it's written, to simulate a heavily loaded cache in production
    .expireAfterWrite(1, TimeUnit.SECONDS)
    .build[String, Value]()


  override def getBytes(key: String) = {
    Option(cache.getIfPresent(key)).map { value =>
      value
    }.getOrElse(null)
  }
}
