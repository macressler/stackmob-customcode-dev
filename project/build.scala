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
    "-Xss32m",
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
    includeFilter in (Compile, filterResources) ~= { f => f || ("*.properties") },
    exportJars := true,
    fork := true,
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    javaOptions in run ++= runArgs,
    javaOptions in test ++= testArgs,
    testOptions in Test += Tests.Argument("html", "console"),
    publishTo <<= version { version: String =>
      val stackmobNexus = "http://nexus/nexus/content/repositories/"
      if (version.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at stackmobNexus + "snapshots/")
      } else {
        Some("releases" at stackmobNexus + "releases/")
      }
    },
    props in Compile <++= (fullClasspath in Runtime).map { cp: Classpath =>
      val artifacts = cp.flatMap(_.get(moduleID.key))
      Seq("DEPENDENCIES" -> artifacts.map(_.toString()).sorted.mkString(","))
    },
    resolvers += "StackMob Nexus" at "http://nexus/nexus/content/groups/public",
    snapshotDependencies ~= { _.filterNot(_.name.contains("scalamachine")) },
    conflictWarning ~= { cw =>
      cw.copy(filter = (id: ModuleID) => true, group = (id: ModuleID) => id.organization + ":" + id.name, level = Level.Error, failOnConflict = true)
    }
  )
}

object Dependencies {
  lazy val specs2             = "org.specs2"              %% "specs2"                   % "1.12.1"          % "test"
  lazy val scalacheck         = "org.scalacheck"          %% "scalacheck"               % "1.10.0"          % "test"
  lazy val mockito            = "org.mockito"             % "mockito-all"               % "1.9.0"           % "test"
  lazy val hamcrest           = "org.hamcrest"            % "hamcrest-all"              % "1.1"             % "test"
  lazy val pegdown            = "org.pegdown"             % "pegdown"                   % "1.0.2"           % "test"
  lazy val h2                 = "com.h2database"          % "h2"                        % "1.2.127"         % "test"
}

object LocalRunnerBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val localRunner = Project("stackmob-customcode-localrunner", file("."),
    settings = standardSettings ++ Seq(
      name := "allspark",
      publish := {}
    ),
    aggregate = Seq(common, server, client)
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