package com.leysoft

import cats.effect.{Blocker, ContextShift, ExitCode, Timer}
import com.leysoft.products.adapter.auth.Auth.{AuthService, InMemoryUserRepository}
import com.leysoft.products.adapter.in.api.{LoginRoute, ProductRoute}
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.doobie.DoobieProductRepository
import com.leysoft.products.adapter.out.doobie.config.DoobieConfiguration
import com.leysoft.products.adapter.out.doobie.util.Doobie
import com.leysoft.products.application.DefaultProductService
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS

object ApiMonix extends TaskApp {
  import com.leysoft.products.adapter.config._
  import org.http4s.implicits._ // for orNotFound
  import cats.syntax.semigroupk._ // for <+>
  implicit val contextShift: ContextShift[Task] = Task.contextShift(scheduler)
  implicit val timer: Timer[Task] = Task.timer(Scheduler.io())

  override def run(args: List[String]): Task[ExitCode] =
    Blocker[Task].use { blocker =>
      DoobieConfiguration[Task]
        .transactor(blocker)
        .use { transactor =>
          Doobie
            .make[Task](transactor)
            .flatMap { implicit doobie =>
              for {
                conf <- config.load[Task]
                repository <- DoobieProductRepository.make[Task]
                service <- DefaultProductService.make[Task](repository)
                error <- ErrorHandler.make[Task]
                handler <- error.handler
                userRepository <- InMemoryUserRepository.make[Task]
                auth <- AuthService.make[Task](conf.auth, userRepository)
                middleware <- auth.middleware
                login <- LoginRoute.make[Task](auth)
                api <- ProductRoute.make[Task](service)
                _ <- BlazeServerBuilder[Task]
                       .bindHttp(
                         port = conf.api.port.value,
                         host = conf.api.host.value
                       )
                       .withHttpApp(
                         CORS {
                           (api.routes(middleware, handler) <+> login
                             .routes(handler)).orNotFound
                         }
                       )
                       .serve
                       .compile
                       .drain
              } yield ExitCode.Success
            }
        }
    }
}
