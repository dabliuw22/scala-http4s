package com.leysoft

import cats.effect.{ContextShift, ExitCode, IO, Timer}
import com.leysoft.products.adapter.auth.Auth.{AuthService, InMemoryUserRepository}
import com.leysoft.products.adapter.auth.LoginRoute
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
  import cats.syntax.semigroupk._ // for <+>
  implicit val contextShift: ContextShift[Task] = Task.contextShift(scheduler)
  implicit val timer: Timer[Task] = Task.timer(Scheduler.io())

  override def run(args: List[String]): Task[ExitCode] =
    DoobieConfiguration[Task].transactor
      .use { transactor =>
        for {
          userRepository <- InMemoryUserRepository.make[Task]
          auth <- AuthService.make[Task](userRepository)
          middleware <- auth.middleware
          db <- HikariDoobieUtil.make[Task](transactor)
          repository <- DoobieProductRepository.make[Task](db)
          service <- DefaultProductService.make[Task](repository)
          login <- LoginRoute.make[Task](auth)
          api <- ProductRoute.make[Task](service)
          error <- ErrorHandler.make[Task]
          _ <- BlazeServerBuilder[Task]
                .bindHttp(port = 8080, host = "localhost")
                .withHttpApp(
                  (api.routes(middleware, error.handler) <+> login
                    .routes(error.handler)).orNotFound
                )
                .serve
                .compile
                .drain
        } yield ExitCode.Success
      }
}
