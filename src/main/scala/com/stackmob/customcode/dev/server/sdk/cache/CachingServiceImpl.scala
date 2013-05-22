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

package com.stackmob.customcode.dev
package server
package sdk
package cache

import com.stackmob.sdkapi.caching.{Operation, CachingService}
import java.util.concurrent.{TimeUnit, ConcurrentHashMap}
import com.stackmob.sdkapi.caching.exceptions.{RateLimitedException, TTLTooBigException, DataSizeException, TimeoutException}
import scalaz.{Validation, Success, Failure}
import java.lang.{Boolean => JBoolean}
import com.twitter.util.Duration
import simulator.{ThrowableFrequency, Frequency, ErrorSimulator}
import CachingServiceImpl._

class CachingServiceImpl(getRateLimitThrowableFreq: ThrowableFrequency = DefaultGetRateLimitThrowableFreq,
                         setRateLimitThrowableFreq: ThrowableFrequency = DefaultSetRateLimitThrowableFreq,
                         getTimeoutThrowableFreq: ThrowableFrequency = DefaultGetTimeoutThrowableFreq,
                         setTimeoutThrowableFreq: ThrowableFrequency = DefaultSetTimeoutThrowableFreq)
  extends CachingService {

  private val maxKeySizeBytes = 1025 //1kb
  private val maxValueSizeBytes = 16384 //16kb
  private val maxSize = 1000
  private type Value = (Array[Byte], Long)
  private val cache = new ConcurrentHashMap[String, Value]()

  case class CacheTooBigException() extends Exception("the cache is too big")
  case class NoSuchKeyException(key: String) extends Exception("no such key %s".format(key))
  case class NeedsRemovalException(key: String) extends Exception("%s needs to be removed from the cache".format(key))

  private def checkCacheSize: Validation[CacheTooBigException, Unit] = {
    if(cache.size > maxSize) {
      Failure(CacheTooBigException())
    } else {
      Success(())
    }
  }

  private def checkKeySize(operation: Operation, key: String): Validation[DataSizeException, Unit] = {
    if(key.length > maxKeySizeBytes) {
      Failure(new DataSizeException(operation))
    } else {
      Success(())
    }
  }

  private def checkValueSize(operation: Operation, value: Array[Byte]): Validation[DataSizeException, Unit] = {
    if (value.size > maxValueSizeBytes) {
      Failure(new DataSizeException(operation))
    } else {
      Success(())
    }
  }

  private def checkKeyNeedsRemoval(key: String, ttl: Long): Validation[NeedsRemovalException, Unit] = {
    if(ttl > System.currentTimeMillis()) {
      Failure(NeedsRemovalException(key))
    } else {
      Success(())
    }
  }

  private def optionToValidation[FailType, SuccessType](mbSuccess: Option[SuccessType],
                                                        fail: FailType): Validation[FailType, SuccessType] = {
    mbSuccess.map { success =>
      Success[FailType, SuccessType](success)
    }.getOrElse {
      Failure[FailType, SuccessType](fail)
    }
  }

  @throws(classOf[TimeoutException])
  @throws(classOf[RateLimitedException])
  @throws(classOf[DataSizeException])
  override def getBytes(key: String): Array[Byte] = {
    cache.synchronized {
      ErrorSimulator(getRateLimitThrowableFreq :: getTimeoutThrowableFreq :: Nil) {
        val v = for {
          _ <- checkKeySize(Operation.GET, key)
          value <- optionToValidation(Option(cache.get(key)), NoSuchKeyException(key))
          _ <- checkKeyNeedsRemoval(key, value._2)
          _ <- checkValueSize(Operation.SET, value._1)
        } yield {
          value
        }

        v.map { value =>
          value._1
        } valueOr {
          case _: NoSuchKeyException => {
            null: Array[Byte]
          }
          case _: NeedsRemovalException => {
            cache.remove(key)
            null: Array[Byte]
          }
          case otherEx: Throwable => {
            (throw otherEx): Array[Byte]
          }
        }
      }
    }
  }

  @throws(classOf[TimeoutException])
  @throws(classOf[RateLimitedException])
  @throws(classOf[DataSizeException])
  @throws(classOf[TTLTooBigException])
  override def setBytes(key: String, value: Array[Byte], ttlMilliseconds: Long): JBoolean = {
    cache.synchronized {
      ErrorSimulator(setRateLimitThrowableFreq :: setTimeoutThrowableFreq :: Nil) {
        val v = for {
          _ <- checkKeySize(Operation.SET, key)
          _ <- checkCacheSize
          _ <- checkValueSize(Operation.SET, value)
          expTime <- Success(System.currentTimeMillis() + ttlMilliseconds)
          _ <- Success(cache.put(key, value -> expTime))
        } yield {
          ()
        }
        v.map { _ =>
          true: JBoolean
        } valueOr {
          case t: CacheTooBigException => false
          case t => throw t
        }
      }
    }
  }

  @throws(classOf[DataSizeException])
  override def deleteEventually(key: String) {
    cache.synchronized {
      val v = for {
        _ <- checkKeySize(Operation.DELETE, key)
        _ <- Success(cache.remove(key))
      } yield {
        ()
      }

      v getOrElse { t: Throwable =>
        throw t
      }
    }
  }
}

object CachingServiceImpl {
  val DefaultGetRateLimitThrowableFreq = ThrowableFrequency(new RateLimitedException(Operation.GET), Frequency(1, Duration(1, TimeUnit.MINUTES)))
  val DefaultSetRateLimitThrowableFreq = ThrowableFrequency(new RateLimitedException(Operation.SET), Frequency(1, Duration(1, TimeUnit.MINUTES)))
  val DefaultGetTimeoutThrowableFreq = ThrowableFrequency(new TimeoutException(Operation.GET), Frequency(1, Duration(1, TimeUnit.MINUTES)))
  val DefaultSetTimeoutThrowableFreq = ThrowableFrequency(new TimeoutException(Operation.SET), Frequency(1, Duration(1, TimeUnit.MINUTES)))
}
