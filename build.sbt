import Dependencies._

name := "sensors-sample-ask"
organization := "hwaitt"
version := "0.0.1"

scalaVersion := "2.13.8"
libraryDependencies ++= (akka ++ circe :+ pureconfig :+ logback :+ slf4jApi)
