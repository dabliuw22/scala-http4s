package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.products.adapter.auth.Auth.{AuthService, InMemoryUserRepository}
import com.leysoft.products.adapter.auth.LoginRoute
import com.leysoft.products.adapter.in.api.ProductRoute
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.skunk.SkunkProductRepository
import com.leysoft.products.adapter.out.skunk.config.SkunkConfiguration
import com.leysoft.products.application.DefaultProductService
import natchez.Trace.Implicits.noop
import eu.timepit.refined.auto._
import org.http4s.server.blaze.BlazeServerBuilder

object ApiCats extends IOApp {
  import com.leysoft.products.adapter.config._
  import org.http4s.implicits._ // for orNotFound
  import cats.syntax.semigroupk._ // for <+>
  // import org.http4s._ // for Request, Response, HttpRoutes
  // import org.http4s.dsl.io._ // for NotFound, Conflict, InternalServerError, Http4sDsl[IO]

  override def run(args: List[String]): IO[ExitCode] =
    SkunkConfiguration[IO].session
      .use { resource =>
        resource.use { session =>
          for {
            conf <- config.load[IO]
            repository <- SkunkProductRepository.make[IO](session)
            service <- DefaultProductService.make[IO](repository)
            error <- ErrorHandler.make[IO]
            handler <- error.handler
            userRepository <- InMemoryUserRepository.make[IO]
            auth <- AuthService.make[IO](conf.auth, userRepository)
            middleware <- auth.middleware
            login <- LoginRoute.make[IO](auth)
            api <- ProductRoute.make[IO](service)
            _ <- BlazeServerBuilder[IO]
                  .bindHttp(port = conf.api.port.value,
                            host = conf.api.host.value)
                  .withHttpApp(
                    (api.routes(middleware, handler) <+> login
                      .routes(handler)).orNotFound
                  )
                  .serve
                  .compile
                  .drain
          } yield ExitCode.Success
        }
      }
}
