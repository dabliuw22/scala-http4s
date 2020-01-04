package com.leysoft.products.adapter.in.api

import cats.Monad
import cats.effect.{Async, ContextShift, Effect}
import com.leysoft.products.domain.Product
import com.leysoft.products.application.ProductService
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

final case class ProductRoute[P[_]: Async: Monad](productService: ProductService[P])(implicit cf: ContextShift[P]) extends Http4sDsl[P] {
  import org.http4s.circe.CirceEntityEncoder._ // for EntityEncoder
  import org.http4s.circe.CirceEntityDecoder._ // for EntityDecoder
  import io.circe.generic.auto._ // for Encoder
  import io.circe.syntax._ // for asJson
  import cats.syntax.applicativeError._ // for recoverWith and handleErrorWith
  import cats.syntax.functor._ // for map
  import cats.syntax.flatMap._ // for flatMap

  private val PRODUCTS = "products"

  def routes(implicit errorHandler: PartialFunction[Throwable, P[Response[P]]]): HttpRoutes[P] = HttpRoutes.of[P] {
    case GET -> Root / PRODUCTS => productService.getAll
      .map(_.asJson)
      .flatMap(Ok(_))
      .recoverWith { errorHandler }
    case GET -> Root / PRODUCTS / LongVar(productId) => productService.get(productId)
      .map(_.asJson)
      .flatMap(Ok(_))
      .handleErrorWith { errorHandler }
    case request @ POST -> Root / PRODUCTS => request.as[Product]
      .flatMap { productService.create }
      .flatMap { Created(_) }
      .handleErrorWith { errorHandler }
    case request @ PUT -> Root / PRODUCTS => request.as[Product]
      .flatMap { productService.update }
      .flatMap { Ok(_) }
      .handleErrorWith { errorHandler }
    case DELETE -> Root / PRODUCTS / LongVar(productId) => productService.remove(productId)
      .map(_.asJson)
      .flatMap(Ok(_))
      .handleErrorWith { errorHandler }
  }
}
