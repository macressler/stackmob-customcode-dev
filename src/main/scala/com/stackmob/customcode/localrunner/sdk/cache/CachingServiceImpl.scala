package com.stackmob.customcode.localrunner.sdk.cache

import com.stackmob.sdkapi.caching.{Operation, CachingService}
import java.util.concurrent.ConcurrentHashMap
import com.stackmob.sdkapi.caching.exceptions.{RateLimitedException, TTLTooBigException, DataSizeException, TimeoutException}
import scalaz.{Validation, Success, Failure}
import java.lang.{Boolean => JBoolean}

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

  //TODO: simulate timeouts

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
  override def getBytes(key: String): Array[Byte] = cache.synchronized {
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
    } ||| {
      case t: NoSuchKeyException => null
      case t: NeedsRemovalException => {
        cache.remove(key)
        null
      }
      case t => throw t
    }
  }

  @throws(classOf[TimeoutException])
  @throws(classOf[RateLimitedException])
  @throws(classOf[DataSizeException])
  @throws(classOf[TTLTooBigException])
  override def setBytes(key: String, value: Array[Byte], ttlMilliseconds: Long): JBoolean = cache.synchronized {
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
    } ||| {
      case t: CacheTooBigException => false
      case t => throw t
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

      v ||| { t =>
        throw t
      }
    }
  }
}
