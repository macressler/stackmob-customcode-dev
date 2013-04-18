import com.github.retronym.SbtOneJar._
import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import sbtfilter.Plugin._
import FilterKeys._
import sbt._
import Keys._

object BuildSettings {
  val org = "com.stackmob"
  val scalaVsn = "2.10.1"

  val defaultArgs = Seq(
    "-Xmx2048m",
    "-XX:MaxPermSize=512m",
    "-Xss32m"
  )
  val runArgs = defaultArgs ++ Seq(
    "-Xdebug",
    "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
  )

  lazy val standardSettings = Defaults.defaultSettings ++ releaseSettings ++ filterSettings ++ Plugin.graphSettings ++ ScalastylePlugin.Settings ++ Seq(
    organization := org,
    scalaVersion := scalaVsn,
    shellPrompt <<= ShellPrompt.prompt,
    filterDirectoryName := "resources",
    exportJars := true,
    fork := true,
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    javaOptions in run ++= runArgs,
    testOptions in Test += Tests.Argument("html", "console"),
    conflictWarning ~= { cw =>
      cw.copy(filter = (id: ModuleID) => true, group = (id: ModuleID) => id.organization + ":" + id.name, level = Level.Error, failOnConflict = true)
    }
  )
}

object Dependencies {
  private lazy val customCodeVsn = "0.5.6"
  private lazy val jettyVsn = "7.5.4.v20111024"
  lazy val customcode = "com.stackmob" % "customcode" % customCodeVsn
  lazy val twitterUtil = "com.twitter" %% "util-core" % "6.3.0"
  lazy val scalaz = "org.scalaz" %% "scalaz-core" % "6.0.4"
  lazy val specs2 = "org.specs2" %% "specs2" % "1.12.3" % "test"
  lazy val jettyServer = "org.eclipse.jetty" % "jetty-server" % jettyVsn
  lazy val akka = "com.typesafe.akka" %% "akka-actor" % "2.1.2"
  lazy val liftJson = "net.liftweb" %% "lift-json" % "2.5-RC2"
  lazy val javaClientSDK = "com.stackmob" % "stackmob-java-client-sdk" % "1.2.0"
  lazy val newman = "com.stackmob" %% "newman" % "0.14.0" exclude("commons-codec", "commons-codec")
  lazy val liftJsonScalaz = "net.liftweb" %% "lift-json-scalaz" % "2.5-RC2"
  lazy val slf4j = "org.slf4j" % "slf4j-api" % "1.7.2"
  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.0.9"
  lazy val mockito = "org.mockito" % "mockito-core" % "1.9.5" % "test"
}

object LocalRunnerBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val localRunner = Project("stackmob-customcode-localrunner", file("."),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq(javaClientSDK,
        customcode,
        scalaz,
        specs2,
        jettyServer,
        liftJson,
        newman,
        liftJsonScalaz,
        slf4j,
        logbackClassic,
        mockito,
        twitterUtil,
        akka),
      name := "stackmob-customcode-localrunner",
      publish := {}
    )
  )
}

object ShellPrompt {
  val prompt = name(name => { state: State =>
    object devnull extends ProcessLogger {
      override def info(s: => String) {}
      override def error(s: => String) { }
      override def buffer[T](f: => T): T = f
    }
    val current = """\*\s+(\w+)""".r
    def gitBranches = ("git branch --no-color" lines_! devnull mkString)
    "%s | %s> " format (
      name,
      current findFirstMatchIn gitBranches map (_.group(1)) getOrElse "-"
      )
  })
}