package com.leysoft.products.adapter.in.api

import cats.effect.{Async, Effect}
import com.leysoft.products.domain.Product
import com.leysoft.products.application.ProductService
import fs2.Stream
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

final class ProductRoute[P[_]: Effect] private (productService: ProductService[P]) extends Http4sDsl[P] {
  import com.leysoft.products.adapter.in.api.ProductRoute.StreamArray
  import org.http4s.circe.CirceEntityEncoder._ // for Generic EntityEncoder
  import org.http4s.circe.CirceEntityDecoder._ // for Generic EntityDecoder
  // import io.circe.generic.auto._ // for Generic (case class) Encoder
  import Codecs._ // for Custom Stream EntityEncoder, Custom Encoder and Custom Decoder
  import io.circe.syntax._ // for asJson
  import cats.syntax.applicativeError._ // for recoverWith and handleErrorWith
  import cats.syntax.functor._ // for map
  import cats.syntax.flatMap._ // for flatMap
  import org.http4s.implicits._ // for orNotFound

  private val PRODUCTS = "products"

  def routes(implicit errorHandler: PartialFunction[Throwable, P[Response[P]]]) = HttpRoutes.of[P] {
    case GET -> Root / PRODUCTS => productService.getAll
      .map(_.asJson)
      .flatMap(Ok(_))
      .recoverWith(errorHandler)
    case GET -> Root / PRODUCTS / "streams" => StreamArray.make(productService.getAllStreams)
      .flatMap(Ok(_))
      .recoverWith(errorHandler)
    case GET -> Root / PRODUCTS / UUIDVar(productId) => productService.get(productId.toString)
      .map(_.asJson)
      .flatMap(Ok(_))
      .handleErrorWith(errorHandler)
    case request @ POST -> Root / PRODUCTS => request.as[Product]
      .flatMap(productService.create)
      .flatMap(Created(_))
      .handleErrorWith(errorHandler)
    case request @ PUT -> Root / PRODUCTS / UUIDVar(productId) => request.as[Product]
      .map(product => product.copy(id = productId.toString))
      .flatMap(productService.update)
      .flatMap(Ok(_))
      .handleErrorWith(errorHandler)
    case DELETE -> Root / PRODUCTS / UUIDVar(productId) => productService.remove(productId.toString)
      .map(_.asJson)
      .flatMap(Ok(_))
      .handleErrorWith(errorHandler)
  }.orNotFound
}

object ProductRoute {

  case class StreamArray[P[_]: Effect, E](stream: Stream[P, E])

  object StreamArray {

     def make[P[_]: Effect, E](stream: Stream[P, E]): P[StreamArray[P, E]] =
       Effect[P].delay(StreamArray(stream))
  }

  def make[P[_]: Effect](service: ProductService[P])(): P[ProductRoute[P]] =
    Effect[P].delay(new ProductRoute[P](service))
}
