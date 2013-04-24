package com.stackmob.customcode.dev
package localrunner

import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import java.util.UUID
import scala.util.Try

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.dev.localrunner.config
 *
 * User: aaron
 * Date: 4/22/13
 * Time: 5:30 PM
 */
package object config {
  class ConfigFile(val apiKey: String,
                   val apiSecret: String,
                   val loggedInUsername: String,
                   val appName: String,
                   val userSchemaName: String,
                   val userName: String,
                   val serverPort: Int)
  object ConfigFile {
    def apply(jsonString: String): Result[ConfigFile] = {
      for {
        jValue <- validating(parse(jsonString))
        configFile <- apply(jValue)
      } yield {
        configFile
      }
    }

    def apply(json: JValue): Result[ConfigFile] = {
      fromJSON[ConfigFile](json)
    }
  }

  private def fmt(s: String)(implicit session: UUID) = "cc-dev-%s-%s".format(s, session)

  implicit def ConfigFileJSONR(implicit session: UUID): JSONR[ConfigFile] = new JSONR[ConfigFile] {
    override def read(json: JValue): Result[ConfigFile] = {
      for {
        apiKey <- field[String]("api-key")(json)
        apiSecret <- field[String]("api-secret")(json)
        mbLoggedInUserName <- field[Option[String]]("logged-in-username")(json)
        mbServerPort <- field[Option[Int]]("server-port")(json)
        mbAppName <- field[Option[String]]("app-name")(json)
        mbUserSchemaName <- field[Option[String]]("user-schema-name")(json)
        mbUserName <- field[Option[String]]("user-name")(json)
      } yield {
        val loggedInUserName = mbLoggedInUserName.getOrElse(fmt("logged-in-user"))
        val serverPort = mbServerPort.getOrElse(8080)
        val appName = mbAppName.getOrElse(fmt("appname"))
        val userSchemaName = mbUserSchemaName.getOrElse(fmt("user-schema-name"))
        val userName = mbUserName.getOrElse(fmt("user-name"))

        new ConfigFile(apiKey, apiSecret, loggedInUserName, appName, userSchemaName, userName, serverPort)
      }
    }

  }
