package com.leysoft.products.adapter.in.api

import cats.Monad
import cats.effect.{Async, ContextShift, Effect}
import com.leysoft.products.application.ProductService
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

final case class ProductRoute[P[_]: Async: Monad](productService: ProductService[P])(implicit cf: ContextShift[P]) extends Http4sDsl[P] {
  import org.http4s.circe.CirceEntityEncoder._ // for EntityEncoder
  import io.circe.generic.auto._ // for Encoder
  import io.circe.syntax._ // for asJson
  import cats.syntax.applicativeError._ // for recoverWith and handleErrorWith
  import cats.syntax.functor._ // for map

  def routes(implicit errorHandler: PartialFunction[Throwable, P[Response[P]]]): HttpRoutes[P] = HttpRoutes.of[P] {
    case GET -> Root / "products" => Ok(productService.getAll.map(_.asJson))
      .recoverWith { errorHandler }
    case GET -> Root / "products" / LongVar(productId) => Ok(productService.get(productId).map(_.asJson))
      .handleErrorWith { errorHandler }
  }
}
