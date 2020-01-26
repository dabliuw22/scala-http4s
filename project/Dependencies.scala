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

  def cats(artifact: String): ModuleID = "org.typelevel" %% artifact % "2.0.0"
  def monix(artifact: String): ModuleID = "io.monix" %% artifact % "3.1.0"
  def doobie(artifact: String): ModuleID = "org.tpolecat" %% artifact % "0.8.6"
  def skunk(artifact: String): ModuleID = "org.tpolecat" %% artifact % "0.0.7"
  def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % "0.21.0-SNAPSHOT"
  def circe(artifact: String): ModuleID = "io.circe" %% artifact % "0.12.3"
  def log4cats(artifact: String): ModuleID = "io.chrisdavenport" %% artifact % "1.0.1"
  def scalaLog(artifact: String): ModuleID = "com.typesafe.scala-logging" %% artifact % "3.9.2"
  def logback(artifact: String): ModuleID = "ch.qos.logback" % artifact % "1.2.3"
  def logbackEncoder(artifact: String): ModuleID = "net.logstash.logback" % artifact % "6.3"

  val dependencies = Seq(
    cats("cats-macros"),
    cats("cats-kernel"),
    cats("cats-core"),
    cats("cats-effect"),
    monix("monix-eval"),
    monix("monix-execution"),
    doobie("doobie-core"),
    doobie("doobie-hikari"),
    doobie("doobie-postgres"),
    skunk("skunk-core"),
    http4s("http4s-dsl"),
    http4s("http4s-blaze-server"),
    http4s("http4s-circe"),
    circe("circe-generic"),
    circe("circe-literal"),
    scalaLog("scala-logging"),
    logback("logback-classic"),
    logbackEncoder("logstash-logback-encoder"),
    log4cats("log4cats-core"),
    log4cats("log4cats-slf4j")
  )

  val testDependencies = Seq(
    scalaTestParent %% "scalatest" % scalaTestVersion % Test,
    scalaMockParent %% "scalamock" % scalaMockVersion % Test,
    scalaCheckParent %% "scalacheck" % scalaCheckVersion % Test,
    scalaTestPlusParent %% "scalacheck-1-14" % scalaTestPlusVersion % Test
  )
}
