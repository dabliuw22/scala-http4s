import sbt._

object Dependencies {
  lazy val scalaTestParent = "org.scalatest"
  lazy val scalaTestVersion = "3.0.8"
  lazy val scalaMockParent = "org.scalamock"
  lazy val scalaMockVersion = "4.4.0"
  lazy val scalaCheckParent = "org.scalacheck"
  lazy val scalaCheckVersion = "1.14.3"
  lazy val scalaTestPlusParent = "org.scalatestplus"
  lazy val scalaTestPlusVersion = "3.1.0.1"
  lazy val testContainersParent = "org.testcontainers"
  lazy val testContainersVersion = "1.14.3"
  lazy val scalaTestContainersParent = "com.dimafeng"
  lazy val scalaTestContainersVersion = "0.37.0"

  def cats(artifact: String): ModuleID = "org.typelevel" %% artifact % "2.0.0"
  def refined(artifact: String): ModuleID = "eu.timepit" %% artifact % "0.9.13"
  def monix(artifact: String): ModuleID = "io.monix" %% artifact % "3.1.0"
  def fs2(artifact: String): ModuleID = "co.fs2" %% artifact % "2.2.1"
  def doobie(artifact: String): ModuleID = "org.tpolecat" %% artifact % "0.8.8"
  def skunk(artifact: String): ModuleID = "org.tpolecat" %% artifact % "0.0.7"
  def redis4Cats(artifact: String): ModuleID =
    "dev.profunktor" %% artifact % "0.9.3"
  def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % "0.21.3"
  def http4sJwtAuth(artifact: String): ModuleID =
    "dev.profunktor" %% artifact % "0.0.4"
  def tracer(artifact: String): ModuleID =
    "dev.profunktor" %% artifact % "1.5.3"
  def circe(artifact: String): ModuleID = "io.circe" %% artifact % "0.12.3"
  def ciris(artifact: String): ModuleID = "is.cir" %% artifact % "1.0.4"
  def log4cats(artifact: String): ModuleID =
    "io.chrisdavenport" %% artifact % "1.0.1"
  def scalaLog(artifact: String): ModuleID =
    "com.typesafe.scala-logging" %% artifact % "3.9.2"
  def logback(artifact: String): ModuleID =
    "ch.qos.logback" % artifact % "1.2.3"
  def logbackEncoder(artifact: String): ModuleID =
    "net.logstash.logback" % artifact % "6.3"

  val dependencies = Seq(
    cats("cats-macros"),
    cats("cats-kernel"),
    cats("cats-core"),
    cats("cats-effect"),
    refined("refined"),
    refined("refined-cats"),
    monix("monix-eval"),
    monix("monix-execution"),
    fs2("fs2-core"),
    doobie("doobie-core"),
    doobie("doobie-hikari"),
    doobie("doobie-postgres"),
    skunk("skunk-core"),
    redis4Cats("redis4cats-effects"),
    redis4Cats("redis4cats-log4cats"),
    http4s("http4s-dsl"),
    http4s("http4s-blaze-server"),
    http4s("http4s-circe"),
    http4sJwtAuth("http4s-jwt-auth"),
    tracer("http4s-tracer"),
    tracer("http4s-tracer-log4cats"),
    circe("circe-generic"),
    circe("circe-literal"),
    circe("circe-parser"),
    ciris("ciris"),
    ciris("ciris-enumeratum"),
    ciris("ciris-refined"),
    scalaLog("scala-logging"),
    logback("logback-classic"),
    logbackEncoder("logstash-logback-encoder"), // for jsonFile
    log4cats("log4cats-core"),
    log4cats("log4cats-slf4j")
  )

  val testDependencies = Seq(
    scalaTestParent %% "scalatest" % scalaTestVersion % Test,
    scalaMockParent %% "scalamock" % scalaMockVersion % Test,
    scalaCheckParent %% "scalacheck" % scalaCheckVersion % Test,
    scalaTestPlusParent %% "scalacheck-1-14" % scalaTestPlusVersion % Test
  )

  val itDependencies = Seq(
    scalaTestParent %% "scalatest" % scalaTestVersion % IntegrationTest,
    scalaCheckParent %% "scalacheck" % scalaCheckVersion % IntegrationTest,
    scalaTestPlusParent %% "scalacheck-1-14" % scalaTestPlusVersion % IntegrationTest,
    // testContainersParent % "testcontainers" % testContainersVersion % IntegrationTest,
    // testContainersParent % "postgresql" % testContainersVersion % IntegrationTest
    scalaTestContainersParent %% "testcontainers-scala-scalatest" % scalaTestContainersVersion % IntegrationTest,
    scalaTestContainersParent %% "testcontainers-scala-postgresql" % scalaTestContainersVersion % IntegrationTest
  )
}
