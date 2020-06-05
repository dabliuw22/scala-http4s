package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.products.adapter.auth.Auth.{AuthService, InMemoryUserRepository}
import com.leysoft.products.adapter.in.api.{DefaultTracedService, LoginRoute, ProductRoute, TracedRoute}
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.skunk.SkunkProductRepository
import com.leysoft.products.adapter.out.skunk.config.SkunkConfiguration
import com.leysoft.products.application.DefaultProductService
import dev.profunktor.tracer.Tracer
import natchez.Trace.Implicits.noop
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS

import scala.concurrent.ExecutionContext.global

object ApiCats extends IOApp {
  import com.leysoft.products.adapter.config._
  import org.http4s.implicits._ // for orNotFound
  import cats.syntax.semigroupk._ // for <+>
  import dev.profunktor.tracer.instances.tracer._ // for Tracer
  import dev.profunktor.tracer.instances.tracerlog._
  // import org.http4s._ // for Request, Response, HttpRoutes
  // import org.http4s.dsl.io._ // for NotFound, Conflict, InternalServerError, Http4sDsl[IO]

  override def run(args: List[String]): IO[ExitCode] =
    SkunkConfiguration[IO].session
      .use { resource =>
        resource.use { implicit session =>
          for {
            conf <- config.load[IO]
            repository <- SkunkProductRepository.make[IO]
            service <- DefaultProductService.make[IO](repository)
            error <- ErrorHandler.make[IO]
            handler <- error.handler
            userRepository <- InMemoryUserRepository.make[IO]
            auth <- AuthService.make[IO](conf.auth, userRepository)
            middleware <- auth.middleware
            login <- LoginRoute.make[IO](auth)
            api <- ProductRoute.make[IO](service)
            traceService <- DefaultTracedService.make[IO]
            traced <- TracedRoute.make[IO](traceService)
            _ <- BlazeServerBuilder[IO](global)
                  .bindHttp(port = conf.api.port.value,
                            host = conf.api.host.value)
                  .withHttpApp(
                    Tracer[IO].loggingMiddleware {
                      CORS {
                        (api.routes(middleware, handler) <+> login
                          .routes(handler) <+> traced
                          .routes(handler)).orNotFound
                      }
                    }
                  )
                  .serve
                  .compile
                  .drain
          } yield ExitCode.Success
        }
      }
}
