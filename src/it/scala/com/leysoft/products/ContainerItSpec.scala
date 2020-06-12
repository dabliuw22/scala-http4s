package com.leysoft.products

import cats.effect.{Blocker, ContextShift, IO, Timer}
import com.dimafeng.testcontainers.ForAllTestContainer
import doobie.util.ExecutionContexts
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.ExecutionContext

trait ContainerItSpec extends AsyncWordSpec with BeforeAndAfterAll with ForAllTestContainer {

  protected implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  protected implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  protected val blocker: Blocker = Blocker.liftExecutionContext(ExecutionContexts.synchronous)
}