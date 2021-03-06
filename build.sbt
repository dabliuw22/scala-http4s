import Dependencies._

lazy val options = Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds", // or import scala.language.higherKinds
  "-Ymacro-annotations" // for newtype and simulacrum
)

lazy val jvmOptions = Seq()

lazy val commonSettings = Seq(
  name := "scala-http4s",
  version := "0.1",
  organization := "com.leysoft",
  scalaVersion := "2.13.1",
  scalacOptions := options,
  javaOptions := jvmOptions,
  fork in Test := true,
  testForkedParallel in Test := true,
  parallelExecution in Test := true,
  fork in IntegrationTest := true,
  testForkedParallel in IntegrationTest := true,
  parallelExecution in IntegrationTest := false,
  scalaSource in Test := baseDirectory.value / "src/test/scala",
  scalaSource in IntegrationTest := baseDirectory.value / "src/it/scala",
  scalafmtOnCompile in ThisBuild := true,
  autoCompilerPlugins in ThisBuild := true,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings))
  .settings(inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings))
  .configs(Test)
  .enablePlugins(FlywayPlugin)
  .settings(inConfig(Test)(Defaults.testSettings))
  .settings(
    libraryDependencies ++= (dependencies ++ testDependencies ++ itDependencies),
    mainClass in Compile := Some("com.leysoft.ApiCats"),
    mainClass in assembly := Some("com.leysoft.ApiCats"),
    assemblyJarName in assembly := "api-cats.jar",
    flywayUrl := sys.env
      .getOrElse("DB_URL", "jdbc:postgresql://localhost:5432/http4s_db"),
    flywayUser := sys.env.getOrElse("DB_USER", "http4s"),
    flywayPassword := sys.env.getOrElse("DB_PASSWORD", "http4s"),
    flywayLocations += "db/migration",
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
    )
  )

addCommandAlias(
  "exec",
  ";clean;update;scalafmtCheckAll;scalafmtSbtCheck;compile;test;it:test"
)
