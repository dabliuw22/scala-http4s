package com.leysoft

import cats.effect.{ContextShift, ExitCode, IO, IOApp, Timer}
import com.leysoft.products.adapter.in.api.ProductRoute
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.doobie.DoobieProductRepository
import com.leysoft.products.adapter.out.doobie.config.DoobieConfiguration
import com.leysoft.products.adapter.out.doobie.util.HikariDoobieUtil
import com.leysoft.products.application.DefaultProductService
import org.http4s.server.blaze.BlazeServerBuilder

object ApiCats extends IOApp {
  // import cats.implicits._ // for <+> and BlazeServerBuilder.as
  // import org.http4s._ // for Request, Response, HttpRoutes
  // import org.http4s.dsl.io._ // for NotFound, Conflict, InternalServerError, Http4sDsl[IO]

  override def run(args: List[String]): IO[ExitCode] = DoobieConfiguration[IO].transactor
    .use { transactor =>
      for {
        db <- HikariDoobieUtil.make[IO](transactor)
        repository <- DoobieProductRepository.make[IO](db)
        service <- DefaultProductService.make[IO](repository)
        api <- ProductRoute.make[IO](service)
        error <- ErrorHandler.make[IO]
        _ <- BlazeServerBuilder[IO]
          .bindHttp(port = 8080, host = "localhost")
          .withHttpApp(api.routes(error.handler))
          .serve
          .compile
          .drain
      } yield ExitCode.Success
    }
}