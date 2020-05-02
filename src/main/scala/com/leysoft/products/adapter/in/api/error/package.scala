package com.leysoft.products.adapter.in.api

import cats.effect.Async
import com.leysoft.products.adapter.auth.Auth.AuthUserException
import com.leysoft.products.domain.error.{ProductNotFoundException, ProductWritingException}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.Response
import org.http4s.dsl.Http4sDsl

package object error {
  import Codecs._

  case class ErrorResponse(message: String)

  class ErrorHandler[P[_]: Async] private () {
    import org.http4s.circe.CirceEntityEncoder._
    import cats.syntax.apply._
    private val dsl: Http4sDsl[P] = Http4sDsl[P]
    import dsl._

    type ExceptionHandler = PartialFunction[Throwable, P[Response[P]]]

    private val logger =
      Slf4jLogger.getLoggerFromClass[P](ErrorHandler.getClass)

    def handler: P[ExceptionHandler] = Async[P].delay {
      case error: ProductNotFoundException =>
        logger.error(s"Error: ${error.getMessage}") *> NotFound(
          ErrorResponse(error.getMessage)
        )
      case error: ProductWritingException =>
        logger.error(s"Error: ${error.getMessage}") *> Conflict(
          ErrorResponse(error.getMessage)
        )
      case error: AuthUserException =>
        logger.error(s"Error: ${error.getMessage}") *> Forbidden(
          ErrorResponse(error.getMessage)
        )
      case error =>
        logger.error(s"Error: ${error.getMessage}") *> InternalServerError(
          ErrorResponse(error.getMessage)
        )
    }
  }

  object ErrorHandler {

    def make[P[_]: Async]: P[ErrorHandler[P]] =
      Async[P].delay(new ErrorHandler[P])
  }

}
