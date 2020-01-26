package com.leysoft.products

import java.util.UUID

import cats.effect.{ContextShift, IO, Timer}
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.concurrent.ExecutionContext

protected[products] trait PropertieSpec extends AsyncFunSuite with ScalaCheckDrivenPropertyChecks {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  private def unique(name: String): String = s"$name - ${UUID.randomUUID()}"

  def spec(testName: String)(f: => IO[Assertion])(implicit position: Position): Unit =
    test(unique(testName))(IO.suspend(f).unsafeToFuture())
}
