package com.leysoft.products.adapter.in.api

import cats.effect.Effect
import com.leysoft.products.adapter.auth.Auth.{AuthService, AuthUser}
import com.leysoft.products.adapter.auth.Codecs
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}

final class LoginRoute[P[_]: Effect] private (auth: AuthService[P])
    extends Http4sDsl[P] {
  import Codecs._
  import cats.syntax.applicativeError._
  import cats.syntax.flatMap._
  import org.http4s.circe.CirceEntityDecoder._
  import org.http4s.circe.CirceEntityEncoder._

  private val PREFIX = "/login"

  private def httpRoutes(
    errorHandler: PartialFunction[Throwable, P[Response[P]]]
  ): HttpRoutes[P] =
    HttpRoutes.of[P] {
      case request @ POST -> Root =>
        request
          .as[AuthUser]
          .flatMap(auth.create)
          .flatMap(Ok(_))
          .handleErrorWith(errorHandler)
    }

  def routes(
    errorHandler: PartialFunction[Throwable, P[Response[P]]]
  ): HttpRoutes[P] =
    Router(PREFIX -> httpRoutes(errorHandler))
}

object LoginRoute {

  def make[P[_]: Effect](auth: AuthService[P]): P[LoginRoute[P]] =
    Effect[P].delay(new LoginRoute[P](auth))
}
