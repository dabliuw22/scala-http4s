package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.products.adapter.in.api.ProductRoute
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.skunk.SkunkProductRepository
import com.leysoft.products.adapter.out.skunk.config.SkunkConfiguration
import com.leysoft.products.application.DefaultProductService
import natchez.Trace.Implicits.noop
import org.http4s.server.blaze.BlazeServerBuilder

object ApiCats extends IOApp {
  import org.http4s.implicits._ // for orNotFound
  // import cats.implicits._ // for <+> and BlazeServerBuilder.as
  // import org.http4s._ // for Request, Response, HttpRoutes
  // import org.http4s.dsl.io._ // for NotFound, Conflict, InternalServerError, Http4sDsl[IO]

  override def run(args: List[String]): IO[ExitCode] =
    SkunkConfiguration[IO].session
      .use { resource =>
        resource.use { session =>
          for {
            repository <- SkunkProductRepository.make[IO](session)
            service <- DefaultProductService.make[IO](repository)
            api <- ProductRoute.make[IO](service)
            error <- ErrorHandler.make[IO]
            _ <- BlazeServerBuilder[IO]
                  .bindHttp(port = 8080, host = "localhost")
                  .withHttpApp(api.routes(error.handler).orNotFound)
                  .serve
                  .compile
                  .drain
          } yield ExitCode.Success
        }
      }
}
