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
  val scalaVsn = "2.9.1"

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
  lazy val mockito = "org.mockito" % "mockito-all" % "1.9.0"
  lazy val scalaz = "org.scalaz" %% "scalaz-core" % "6.0.4"
  lazy val specs2 = "org.specs2" %% "specs2" % "1.12.1" % "test"
  lazy val jettyServer = "org.eclipse.jetty" % "jetty-server" % jettyVsn
  lazy val liftJson = "net.liftweb" %% "lift-json" % "2.5-RC2"
  lazy val javaClientSDK = "com.stackmob" % "stackmob-java-client-sdk" % "1.2.0"
  lazy val guava = "com.google.guava" % "guava" % "14.0.1"
}

object LocalRunnerBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val localRunner = Project("stackmob-customcode-localrunner", file("."),
    settings = standardSettings ++ Seq(
      libraryDependencies ++= Seq(javaClientSDK,
        customcode,
        mockito,
        scalaz,
        specs2,
        jettyServer,
        liftJson,
				guava),
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