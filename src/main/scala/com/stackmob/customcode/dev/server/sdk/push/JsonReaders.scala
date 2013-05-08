package com.stackmob.customcode.dev
package server
package sdk
package push

import com.stackmob.sdkapi.PushService.{TokenAndType, TokenType}
import net.liftweb.json.{JValue, JArray, JObject, parse}
import scalaz.Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.JsonAST.JField

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.server.sdk.push
 * 
 * User: aaron
 * Date: 4/2/13
 * Time: 2:15 PM
 */
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
