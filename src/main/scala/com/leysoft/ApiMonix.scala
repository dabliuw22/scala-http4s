package com.leysoft

import cats.effect.{ContextShift, ExitCode, Timer}
import com.leysoft.products.adapter.in.api.ProductRoute
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.doobie.DoobieProductRepository
import com.leysoft.products.adapter.out.doobie.config.DoobieConfiguration
import com.leysoft.products.adapter.out.doobie.util.HikariDoobieUtil
import com.leysoft.products.application.DefaultProductService
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler
import org.http4s.server.blaze.BlazeServerBuilder

object ApiMonix extends TaskApp {
  import org.http4s.implicits._ // for orNotFound
  implicit val contextShift: ContextShift[Task] = Task.contextShift(scheduler)
  implicit val timer: Timer[Task] = Task.timer(Scheduler.io())

  override def run(args: List[String]): Task[ExitCode] =
    DoobieConfiguration[Task].transactor
      .use { transactor =>
        for {
          db <- HikariDoobieUtil.make[Task](transactor)
          repository <- DoobieProductRepository.make[Task](db)
          service <- DefaultProductService.make[Task](repository)
          api <- ProductRoute.make[Task](service)
          error <- ErrorHandler.make[Task]
          _ <- BlazeServerBuilder[Task]
                .bindHttp(port = 8080, host = "localhost")
                .withHttpApp(api.routes(error.handler).orNotFound)
                .serve
                .compile
                .drain
        } yield ExitCode.Success
      }
}
