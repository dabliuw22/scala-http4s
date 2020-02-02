package com.leysoft.products.adapter.in.api

import cats.effect.{Async, Effect}
import com.leysoft.products.domain.Product
import com.leysoft.products.application.ProductService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}

final class ProductRoute[P[_]: Effect] private (productService: ProductService[P]) extends Http4sDsl[P] {
  import org.http4s.circe.CirceEntityEncoder._ // for Generic EntityEncoder
  import org.http4s.circe.CirceEntityDecoder._ // for Generic EntityDecoder
  // import io.circe.generic.auto._ // for Generic (case class) Encoder
  import Codecs._ // for Custom Stream EntityEncoder, Custom Encoder and Custom Decoder
  import io.circe.syntax._ // for asJson
  import cats.syntax.applicativeError._ // for recoverWith and handleErrorWith
  import cats.syntax.functor._ // for map
  import cats.syntax.flatMap._ // for flatMap

  private val PREFIX = "/products"

  private def httpRoutes(errorHandler: PartialFunction[Throwable, P[Response[P]]]) = HttpRoutes.of[P] {
    case GET -> Root => productService.getAll
      .map(_.asJson)
      .flatMap(Ok(_))
      .recoverWith(errorHandler)
    case GET -> Root / "streams" => Ok(productService.getAllStreams)
      .recoverWith(errorHandler)
    case GET -> Root / UUIDVar(productId) => productService.get(productId.toString)
      .map(_.asJson)
      .flatMap(Ok(_))
      .handleErrorWith(errorHandler)
    case request @ POST -> Root => request.as[Product]
      .flatMap(productService.create)
      .flatMap(Created(_))
      .handleErrorWith(errorHandler)
    case request @ PUT -> Root / UUIDVar(productId) => request.as[Product]
      .map(product => product.copy(id = productId.toString))
      .flatMap(productService.update)
      .flatMap(Ok(_))
      .handleErrorWith(errorHandler)
    case DELETE -> Root / UUIDVar(productId) => productService.remove(productId.toString)
      .map(_.asJson)
      .flatMap(Ok(_))
      .handleErrorWith(errorHandler)
  }

  def routes(errorHandler: PartialFunction[Throwable, P[Response[P]]]): HttpRoutes[P] =
    Router(PREFIX -> httpRoutes(errorHandler))
}

object ProductRoute {

  def make[P[_]: Effect](service: ProductService[P])(): P[ProductRoute[P]] =
    Effect[P].delay(new ProductRoute[P](service))
}
