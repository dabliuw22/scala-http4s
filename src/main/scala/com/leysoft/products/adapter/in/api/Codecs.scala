package com.leysoft.products.adapter.in.api

import io.circe.{Decoder, Encoder, Json}
import com.leysoft.products.domain.Product
import fs2.Stream
import org.http4s.headers.`Content-Type`
import org.http4s.{Entity, EntityEncoder, Headers, MediaType}

object Codecs {

  private val ID = "id"

  private val NAME = "name"

  private val STOCK = "stock"

  implicit val productDecoder: Decoder[Product] =
    // Decoder.forProduct2(NAME, STOCK)(domain.Product.apply)
    Decoder.instance[Product] { cursor =>
      for {
        name <- cursor.downField(NAME).as[String]
        stock <- cursor.downField(STOCK).as[Double]
      } yield Product.make(name, stock)
    }

  implicit val productEncoder: Encoder[Product] =
    // Encoder.forProduct3(ID, NAME, STOCK)(p => (p.id, p.name, p.stock))
    Encoder.instance[Product] { p =>
      Json.obj(
        (ID, Json.fromString(p.id)),
        (NAME, Json.fromString(p.name)),
        (STOCK, Json.fromDoubleOrString(p.stock))
      )
    }

  implicit def streamEntityEncoder[P[_], E: Encoder]
    : EntityEncoder[P, StreamArray[P, E]] =
    new EntityEncoder[P, StreamArray[P, E]] {

      override def toEntity(a: StreamArray[P, E]): Entity[P] = {
        val stream: Stream[P, String] = Stream.emit("[") ++ a.stream
          .map(Encoder[E].apply)
          .map(_.noSpaces)
          .intersperse(",") ++ Stream.emit("]")
        Entity(stream.through(fs2.text.utf8Encode[P]))
      }

      override def headers: Headers =
        Headers.of(`Content-Type`(MediaType.application.json))
    }
}
