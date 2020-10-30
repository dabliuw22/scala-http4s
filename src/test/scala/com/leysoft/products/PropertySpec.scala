package com.leysoft.products

import java.util.UUID

import cats.effect.{ContextShift, IO, Timer}
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.{ScalaCheckDrivenPropertyChecks, ScalaCheckPropertyChecks}

import scala.concurrent.ExecutionContext

protected[products] abstract class PropertySpec
    extends Spec
    with ScalaCheckPropertyChecks {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  private def unique(name: String): String = s"$name - ${UUID.randomUUID()}"

  def spec(
    testName: String,
    testDetail: String = ""
  )(f: => IO[Assertion])(implicit position: Position): Unit = {
    s"${unique(testName)}" should {
      s"$testDetail" in IO.suspend(f).unsafeToFuture()
    }
  }
}
