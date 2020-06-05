package com.leysoft.products.adapter.in.api

import cats.effect.Effect
import com.leysoft.products.adapter.auth.Auth.User
import com.leysoft.products.domain.Product
import com.leysoft.products.application.ProductService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes, Response}

final class ProductRoute[P[_]: Effect] private (
  productService: ProductService[P]
) extends Http4sDsl[P] {
  import org.http4s.circe.CirceEntityEncoder._ // for Generic EntityEncoder
  import org.http4s.circe.CirceEntityDecoder._ // for Generic EntityDecoder
  // import io.circe.generic.auto._ // for Generic (case class) Encoder
  import Codecs._ // for Custom Stream EntityEncoder, Custom Encoder and Custom Decoder
  import io.circe.syntax._ // for asJson
  import cats.syntax.applicativeError._ // for recoverWith and handleErrorWith
  import cats.syntax.functor._ // for map
  import cats.syntax.flatMap._ // for flatMap

  private val PREFIX = "/products"

  private def httpRoutes(
    errorHandler: PartialFunction[Throwable, P[Response[P]]]
  ): AuthedRoutes[User, P] = AuthedRoutes.of {
    case GET -> Root as user =>
      productService.getAll
        .map(_.asJson)
        .flatMap(Ok(_))
        .recoverWith(errorHandler)
    case GET -> Root / "streams" as user =>
      StreamArray
        .make(productService.getAllStreams)
        .flatMap(Ok(_))
        .recoverWith(errorHandler)
    case GET -> Root / UUIDVar(productId) as user =>
      productService
        .get(productId.toString)
        .map(_.asJson)
        .flatMap(Ok(_))
        .handleErrorWith(errorHandler)
    case request @ POST -> Root as user =>
      request.req
        .as[Product]
        .flatMap(productService.create)
        .flatMap(Created(_))
        .handleErrorWith(errorHandler)
    case request @ PUT -> Root / UUIDVar(productId) as user =>
      request.req
        .as[Product]
        .map(product => product.copy(id = productId.toString))
        .flatMap(productService.update)
        .flatMap(Ok(_))
        .handleErrorWith(errorHandler)
    case DELETE -> Root / UUIDVar(productId) as user =>
      productService
        .remove(productId.toString)
        .map(_.asJson)
        .flatMap(Ok(_))
        .handleErrorWith(errorHandler)
  }

  def routes(
    auth: AuthMiddleware[P, User],
    errorHandler: PartialFunction[Throwable, P[Response[P]]]
  ): HttpRoutes[P] =
    Router(PREFIX -> auth(httpRoutes(errorHandler)))
}

object ProductRoute {

  def make[P[_]: Effect](service: ProductService[P]): P[ProductRoute[P]] =
    Effect[P].delay(new ProductRoute[P](service))
}
