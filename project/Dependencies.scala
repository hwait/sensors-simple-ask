import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {

  val slf4jVersion = "1.7.36"
  val logbackVersion = "1.2.11"
  val pureConfigVersion = "0.17.1"
  val akkaVersion = "2.6.19"
  val akkaHttpVersion = "10.2.9"
  val circeVersion           = "0.14.2"

  lazy val slf4jApi     = "org.slf4j"                    % "slf4j-api"              % slf4jVersion
  lazy val logback      = ("ch.qos.logback"              % "logback-classic"        % logbackVersion).exclude("org.slf4j", "slf4j")
  lazy val pureconfig       = "com.github.pureconfig"       %% "pureconfig"             % pureConfigVersion

  lazy val akka = Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    ).map(_.exclude("org.slf4j", "slf4j"))

  lazy val circe = Seq(
      "io.circe"          %% "circe-core"           % circeVersion,
      "io.circe"          %% "circe-generic"        % circeVersion,
      "io.circe"          %% "circe-parser"         % circeVersion,
      "io.circe"          %% "circe-generic-extras" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe"      % "1.39.2",
    )

}