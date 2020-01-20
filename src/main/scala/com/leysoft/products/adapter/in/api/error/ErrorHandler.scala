package com.leysoft.products.adapter.in.api.error

import cats.effect.Async
import com.leysoft.products.domain.error.{ProductNotFoundException, ProductWritingException}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.Response
import org.http4s.dsl.Http4sDsl

case class ErrorHandler[P[_]: Async] private() {
  val dsl: Http4sDsl[P] = Http4sDsl[P]
  import dsl._
  import cats.syntax.apply._

  private val logger = Slf4jLogger.getLoggerFromClass[P](ErrorHandler.getClass)

  def  handler: PartialFunction[Throwable, P[Response[P]]] = {
    case error: ProductNotFoundException =>
      logger.error(s"Error: ${error.getMessage}") *> NotFound(s"Oops....")
    case error: ProductWritingException =>
      logger.error(s"Error: ${error.getMessage}") *> Conflict(s"Oops....")
    case _ => InternalServerError(s"Oops....")
  }
}

object ErrorHandler {

  def make[P[_]: Async]: P[ErrorHandler[P]] =
    Async[P].delay(ErrorHandler[P])
}
