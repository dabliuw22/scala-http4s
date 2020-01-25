package com.leysoft.products.adapter.in.api

import io.circe.{Decoder, Encoder, Json}
import com.leysoft.products.domain

object Codecs {

  private val ID = "id"

  private val NAME = "name"

  private val STOCK = "stock"

  implicit val productDecoder: Decoder[domain.Product] =
    // Decoder.forProduct2(NAME, STOCK)(domain.Product.apply)
    Decoder.instance[domain.Product] { cursor =>
      for {
        name <- cursor.downField(NAME).as[String]
        stock <- cursor.downField(STOCK).as[Double]
      } yield domain.Product(name, stock)
    }

  implicit val productEncoder: Encoder[domain.Product] =
    // Encoder.forProduct3(ID, NAME, STOCK)(p => (p.id, p.name, p.stock))
    Encoder.instance[domain.Product] { p =>
      Json.obj(
        (ID, Json.fromString(p.id)),
        (NAME, Json.fromString(p.name)),
        (STOCK, Json.fromDoubleOrString(p.stock))
      )
    }
}
