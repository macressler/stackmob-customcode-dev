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

package com.stackmob.customcode

import com.stackmob.sdk.api.StackMob
import com.stackmob.sdk.api.StackMob.OAuthVersion
import com.stackmob.sdk.push.StackMobPush
import net.liftweb.json.{JValue, parse}
import net.liftweb.json.scalaz.JsonScalaz.{Result, JSONR, fromJSON}
import scala.util.Try
import scalaz.{Success => ScalazSuccess, Failure => ScalazFailure, Validation}
import scala.util.Success
import scala.util.Failure

package object dev {
  def stackMobClient(apiKey: String, apiSecret: String): StackMob = {
    new StackMob(OAuthVersion.One, 0, apiKey, apiSecret)
  }

  def stackMobPushClient(apiKey: String, apiSecret: String): StackMobPush = {
    new StackMobPush(0, apiKey, apiSecret)
  }

  type ConfigMap = Map[ConfigKey, ConfigVal]
  val DefaultConfig: ConfigMap = Map(EnableDatastoreService -> Enabled(false))

  implicit class TryW[T](inner: Try[T]) {
    def toEither: Either[Throwable, T] = {
      inner match {
        case Success(successVal) => Right(successVal)
        case Failure(throwable) => Left(throwable)
      }
    }

    def toValidation: Validation[Throwable, T] = {
      inner match {
        case Success(s) => ScalazSuccess(s)
        case Failure(f) => ScalazFailure(f)
      }
    }

    def mapFailure(fn: PartialFunction[Throwable, Throwable]): Try[T] = {
      inner match {
        case s@Success(_) => s
        case Failure(t) => {
          if(fn.isDefinedAt(t)) {
            Failure(fn(t))
          } else {
            Failure(t)
          }
        }
      }
    }
  }

  implicit class JValueW(inner: JValue) {
    def toResult[Res: JSONR]: Result[Res] = {
      fromJSON[Res](inner)
    }
  }

  implicit class StringW(inner: String) {
    def getBytesUTF8: Array[Byte] = {
      inner.getBytes("UTF-8")
    }

    def toJValue: Try[JValue] = {
      Try(parse(inner))
    }
  }

  implicit class ValidationW[Fail, Succ](validation: Validation[Fail, Succ]) {

    def mapFailure[NewFail](fn: Fail => NewFail): Validation[NewFail, Succ] = {
      validation match {
        case ScalazSuccess(s) => ScalazSuccess(s)
        case ScalazFailure(t) => ScalazFailure(fn(t))
      }
    }
  }

  implicit class ThrowableValidationW[S](validation: Validation[Throwable, S]) {
    def getOrThrow: S = {
      validation ||| { t: Throwable =>
        throw t
      }
    }
  }
}
