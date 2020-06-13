import Dependencies._
import ScalafmtPlugin._

lazy val options =  Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds" // or import scala.language.higherKinds
)

lazy val commonSettings = Seq(
  name := "scala-http4s",
  version := "0.1",
  organization := "com.leysoft",
  scalaVersion := "2.13.1",
  scalacOptions := options,
  scalaSource in Test := baseDirectory.value / "src/test/scala",
  scalaSource in IntegrationTest := baseDirectory.value / "src/it/scala",
  scalafmtOnCompile in ThisBuild := true,
  autoCompilerPlugins in ThisBuild := true,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings))
  .settings(inConfig(IntegrationTest)(scalafmtConfigSettings))
  .configs(Test)
  .settings(inConfig(Test)(Defaults.testSettings))
  .settings(
    libraryDependencies ++= (dependencies ++ testDependencies ++ itDependencies),
    mainClass in assembly := Some("com.leysoft.ApiCats"),
    assemblyJarName in assembly := "api-cats.jar"
  )