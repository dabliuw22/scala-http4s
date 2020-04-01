import Dependencies._

name := "scala-http4s"

version := "0.1"

scalaVersion := "2.13.1"

scalafmtOnCompile in ThisBuild := true

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= (dependencies ++ testDependencies)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds" // or import scala.language.higherKinds
)
