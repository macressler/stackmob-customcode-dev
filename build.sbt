import CustomCodeDevReleaseSteps._
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._
import sbt._
import net.virtualvoid.sbt.graph.Plugin
import org.scalastyle.sbt.ScalastylePlugin

name := "stackmob-customcode-dev"

organization := "com.stackmob"

scalaVersion := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= {
    val customCodeVsn = "0.6.3"
    val jettyVsn = "8.1.13.v20130916"
    val newmanVsn = "1.2.2"
    val scalazVsn = "7.0.2"
    Seq (
        "com.stackmob"      % "customcode"                  % customCodeVsn,
        "com.twitter"       %% "util-core"                  % "6.11.1" exclude("org.scala-lang", "scala-library"),
        "org.scalaz"        %% "scalaz-core"                % scalazVsn exclude("org.scala-lang", "scala-library"),
        "org.scalaz"        %% "scalaz-effect"              % scalazVsn exclude("org.scala-lang", "scala-library"),
        "org.scalaz"        %% "scalaz-concurrent"          % scalazVsn exclude("org.scala-lang", "scala-library"),
        "org.eclipse.jetty" % "jetty-server"                % jettyVsn,
        "net.liftweb"       %% "lift-json"                  % "2.5.1" exclude("org.scala-lang", "scala-library"),
        "net.liftweb"       %% "lift-json-scalaz7"          % "2.5.1" exclude("org.scalaz", "scalaz-core_2.10") exclude("org.scala-lang", "scala-library"),
        "com.stackmob"      % "stackmob-java-client-sdk"    % "1.3.7",
        "com.stackmob"      %% "newman"                     % newmanVsn exclude("commons-codec", "commons-codec") exclude("com.twitter", "finagle-http_2.10") exclude("org.scala-lang", "scala-library"),
        "com.google.guava"  % "guava"                       % "14.0.1",
        "org.slf4j"         % "slf4j-api"                   % "1.7.2",
        "ch.qos.logback"    % "logback-classic"             % "1.0.9",
        "org.specs2"        %% "specs2"                     % "2.2.3"   % "test" exclude("org.scala-lang", "scala-library"),
        "org.scalacheck"    %% "scalacheck"                 % "1.10.1"  % "test" exclude("org.scala-lang", "scala-library"),
        "com.stackmob"      %% "newman"                     % newmanVsn % "test" classifier("test"),
        "org.mockito"       % "mockito-all"                 % "1.9.5"   % "test" exclude("org.parboiled", "parboiled-core"),
        "org.pegdown"       % "pegdown"                     % "1.2.1"   % "test" exclude("org.parboiled", "parboiled-core")
    )
}

conflictManager := ConflictManager.strict

logBuffered := false

ScalastylePlugin.Settings

Plugin.graphSettings

releaseSettings

releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    setReadmeReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
)

resolvers ++= List(
    "spray repo" at "http://repo.spray.io",
    "spray nightly" at "http://nightlies.spray.io"
)

publishTo <<= (version) { version: String =>
    val nexus = "https://oss.sonatype.org/"
    if (version.trim.endsWith("SNAPSHOT")) {
        Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
}

publishMavenStyle := true

publishArtifact in Test := true

testOptions in Test += Tests.Argument("html", "console")

pomIncludeRepository := { _ => false }

pomExtra := (
    <url>https://github.com/stackmob/stackmob-customcode-dev</url>
    <licenses>
        <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>
    <scm>
        <url>git@github.com:stackmob/stackmob-customcode-dev.git</url>
        <connection>scm:git:git@github.com:stackmob/stackmob-customcode-dev.git</connection>
    </scm>
    <developers>
        <developer>
            <id>arschles</id>
            <name>Aaron Schlesinger</name>
            <url>http://github.com/arschles</url>
        </developer>
    </developers>
)
