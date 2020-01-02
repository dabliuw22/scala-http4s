import sbt._

object Dependencies {
  lazy val typeLevelParent = "org.typelevel"
  lazy val catsVersion = "2.0.0"
  lazy val doobieParent = "org.tpolecat"
  lazy val doobieVersion = "0.8.6"
  lazy val http4sParent = "org.http4s"
  lazy val http4sVersion = "0.21.0-SNAPSHOT"
  lazy val circeParent = "io.circe"
  lazy val circeVersion = "0.12.3"
  lazy val scalaLoggingParent = "com.typesafe.scala-logging"
  lazy val scalaLoggingVersion = "3.9.2"
  lazy val logbackParent = "ch.qos.logback"
  lazy val logbackVersion = "1.2.3"
  lazy val logbackEncoderParent = "net.logstash.logback"
  lazy val logbackEncoderVersion = "6.3"
  lazy val scalaTestParent = "org.scalatest"
  lazy val scalaTestVersion = "3.0.8"
  lazy val scalaMockParent = "org.scalamock"
  lazy val scalaMockVersion = "4.4.0"

  val dependencies = Seq(
    typeLevelParent %% "cats-macros" % catsVersion,
    typeLevelParent %% "cats-kernel" % catsVersion,
    typeLevelParent %% "cats-core" % catsVersion,
    typeLevelParent %% "cats-effect" % catsVersion,
    doobieParent %% "doobie-core" % doobieVersion,
    doobieParent %% "doobie-hikari" % doobieVersion,
    doobieParent %% "doobie-postgres" % doobieVersion,
    http4sParent %% "http4s-dsl" % http4sVersion,
    http4sParent %% "http4s-blaze-server" % http4sVersion,
    http4sParent %% "http4s-circe" % http4sVersion,
    circeParent %% "circe-generic" % circeVersion,
    circeParent %% "circe-literal" % circeVersion,
    scalaLoggingParent %% "scala-logging" % scalaLoggingVersion,
    logbackParent % "logback-classic" % logbackVersion,
    logbackEncoderParent % "logstash-logback-encoder" % logbackEncoderVersion
  )

  val testDependencies = Seq(
    scalaTestParent %% "scalatest" % scalaTestVersion % Test,
    scalaMockParent %% "scalamock" % scalaMockVersion % Test
  )
}
