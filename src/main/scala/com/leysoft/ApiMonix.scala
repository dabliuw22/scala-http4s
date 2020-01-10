package com.leysoft

import cats.data.Kleisli
import cats.effect.{ContextShift, ExitCode, Timer}
import com.leysoft.products.adapter.in.api.ProductRoute
import com.leysoft.products.adapter.out.doobie.DoobieProductRepository
import com.leysoft.products.adapter.out.doobie.config.DoobieConfiguration
import com.leysoft.products.adapter.out.doobie.util.{DoobieUtil, HikariDoobieUtil}
import com.leysoft.products.application.{DefaultProductService, ProductService}
import com.leysoft.products.domain.ProductRepository
import com.leysoft.products.domain.error.{ProductNotFoundException, ProductWritingException}
import com.typesafe.scalalogging.Logger
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory

object ApiMonix extends TaskApp {
  override protected def scheduler: Scheduler = Scheduler.computation()
  implicit val contextShift: ContextShift[Task] = Task.contextShift(scheduler)
  implicit val timer: Timer[Task] = Task.timer(Scheduler.global)
  val logger = Logger(LoggerFactory.getLogger(ApiMonix.getClass))
  implicit val doobieConfiguration: DoobieConfiguration[Task] = DoobieConfiguration[Task]
  implicit val doobieUtil: DoobieUtil[Task] = HikariDoobieUtil[Task]
  val productRepository: ProductRepository[Task] = DoobieProductRepository[Task]
  val productService: ProductService[Task] = DefaultProductService[Task](productRepository)
  val productRoute: ProductRoute[Task] = ProductRoute[Task](productService)
  import cats.implicits._ // for <+> and BlazeServerBuilder.as
  import org.http4s.implicits._ // for orNotFound
  import org.http4s._ // for Request, Response, HttpRoutes
  val dsl: Http4sDsl[Task] = Http4sDsl[Task]
  import dsl._ // for NotFound, Conflict, InternalServerError

  implicit val errorHandler: PartialFunction[Throwable, Task[Response[Task]]] = {
    case error: ProductNotFoundException =>
      logger.error(s"Error: ${error.getMessage}")
      NotFound(s"Oops....")
    case error: ProductWritingException =>
      logger.error(s"Error: ${error.getMessage}")
      Conflict(s"Oops....")
    case _ => InternalServerError(s"Oops....")
  }

  val helloWorldRoute: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name")
  }

  val routes: Kleisli[Task, Request[Task], Response[Task]] = (productRoute.routes <+> helloWorldRoute).orNotFound

  override def run(args: List[String]): Task[ExitCode] = BlazeServerBuilder[Task]
    .bindHttp(port = 8080, host = "localhost")
    .withHttpApp(routes)
    .serve
    .compile
    .drain
    .as(ExitCode.Success)
}
