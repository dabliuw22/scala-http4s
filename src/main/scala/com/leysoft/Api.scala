package com.leysoft

import akka.dispatch.ExecutionContexts
import cats.data.Kleisli
import cats.effect.{ContextShift, ExitCode, IO, IOApp, Timer}
import com.leysoft.products.adapter.in.api.ProductRoute
import com.leysoft.products.adapter.out.doobie.DoobieProductRepository
import com.leysoft.products.adapter.out.doobie.config.DoobieConfiguration
import com.leysoft.products.adapter.out.doobie.util.{DoobieUtil, HikariDoobieUtil}
import com.leysoft.products.application.{DefaultProductService, ProductService}
import com.leysoft.products.domain.ProductRepository
import com.typesafe.scalalogging.Logger
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory

object Api extends IOApp {
  override  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContexts.global)
  override implicit val timer: Timer[IO] = IO.timer(ExecutionContexts.global)
  val logger = Logger(LoggerFactory.getLogger(Api.getClass))
  implicit val doobieConfiguration: DoobieConfiguration[IO] = DoobieConfiguration[IO]
  implicit val doobieUtil: DoobieUtil[IO] = HikariDoobieUtil[IO]
  val productRepository: ProductRepository[IO] = DoobieProductRepository[IO]
  val productService: ProductService[IO] = DefaultProductService[IO](productRepository)
  val productRoute: ProductRoute[IO] = ProductRoute[IO](productService)
  import cats.implicits._ // for <+> and BlazeServerBuilder.as
  import org.http4s.implicits._ // for orNotFound
  import org.http4s._, org.http4s.dsl.io._
  val helloWorldRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name")
  }
  val routes: Kleisli[IO, Request[IO], Response[IO]] = (productRoute.routes <+> helloWorldRoute).orNotFound

  override def run(args: List[String]): IO[ExitCode] = BlazeServerBuilder[IO]
    .bindHttp(port = 8080, host = "localhost")
    .withHttpApp(routes)
    .serve
    .compile
    .drain
    .as(ExitCode.Success)
}