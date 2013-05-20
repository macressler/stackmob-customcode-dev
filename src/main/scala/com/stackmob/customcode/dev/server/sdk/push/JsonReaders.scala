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
package push

import com.stackmob.sdkapi.PushService.{TokenAndType, TokenType}
import net.liftweb.json.{JValue, JArray, JObject, parse}
import scalaz.Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.JsonAST.JField

trait JsonReaders {

  implicit val tokenAndTypeJSONR = new JSONR[TokenAndType] {
    override def read(json: JValue): Result[TokenAndType] = {
      val tokenResult = field[String]("token")(json)
      val typeResult = field[String]("type")(json).flatMap { typeString =>
        validating(TokenType.valueOf(typeString)).mapFailure { t =>
          nel(UncategorizedError("unknown token type", "%s isn't a valid token type".format(typeString), Nil))
        }
      }
      for {
        token <- tokenResult
        tokenType <- typeResult
      } yield {
        tokenAndType(token, tokenType)
      }
    }
  }

  implicit def mapJsonR[K: JSONR, V: JSONR]: JSONR[Map[K, V]] = new JSONR[Map[K, V]] {
    override def read(json: JValue): Result[Map[K, V]] = {
      json match {
        case JObject(fields) => {
          val listOfResults: List[Result[(K, V)]] = fields.map { jField =>
            for {
              fieldNameJValue <- validating(parse(jField.name)).mapFailure { t =>
                nel(UncategorizedError("invalid JSON %s".format(jField.name), t.getMessage, Nil))
              }
              key <- fromJSON[K](fieldNameJValue)
              value <- fromJSON[V](jField.value)
            } yield key -> value
          }
          listOfResults.sequence[Result, (K, V)].map { listOfTuples: List[(K, V)] =>
            listOfTuples.toMap
          }
        }
        case otherJValue => UnexpectedJSONError(otherJValue, classOf[JObject]).fail.liftFailNel
      }
    }
  }

  implicit def listJsonR[T: JSONR]: JSONR[List[T]] = new JSONR[List[T]] {
    override def read(json: JValue): Result[List[T]] = {
      json match {
        case JArray(arr) => {
          val listOfResults = arr.map { jvalue =>
            val valueResult = fromJSON[T](jvalue)
            valueResult
          }
          listOfResults.sequence[Result, T]
        }
        case otherJValue => UnexpectedJSONError(otherJValue, classOf[JArray]).fail.liftFailNel
      }
    }
  }
}
