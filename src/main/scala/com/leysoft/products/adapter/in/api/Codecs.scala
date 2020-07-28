package com.leysoft.products.adapter.in.api

import io.circe.{Decoder, Encoder, Json}
import com.leysoft.products.domain
import fs2.Stream
import org.http4s.headers.`Content-Type`
import org.http4s.{Entity, EntityEncoder, Headers, MediaType}

object Codecs {

  private val ID = "id"

  private val NAME = "name"

  private val STOCK = "stock"

  private val CREATED_AT = "created_at"

  implicit val productDecoder: Decoder[domain.Product] =
    // Decoder.forProduct2(NAME, STOCK)(domain.Product.apply)
    Decoder.instance[domain.Product] { cursor =>
      for {
        name <- cursor.downField(NAME).as[String]
        stock <- cursor.downField(STOCK).as[Double]
      } yield domain.Product.make(name, stock)
    }

  implicit val productEncoder: Encoder[domain.Product] =
    // Encoder.forProduct3(ID, NAME, STOCK)(p => (p.id, p.name, p.stock))
    Encoder.instance[domain.Product] { p =>
      Json.obj(
        (ID, Json.fromString(p.id)),
        (NAME, Json.fromString(p.name)),
        (STOCK, Json.fromDoubleOrString(p.stock)),
        (CREATED_AT, Json.fromString(p.createdAt.toString))
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
