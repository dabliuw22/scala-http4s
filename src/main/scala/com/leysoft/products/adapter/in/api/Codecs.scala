package com.leysoft.products.adapter.in.api

import io.circe.{Decoder, Encoder}
import com.leysoft.products.domain

object Codecs {

  private val ID = "id"

  private val NAME = "name"

  private val STOCK = "stock"

  implicit val productDecoder: Decoder[domain.Product] =
    Decoder.forProduct2(NAME, STOCK)(domain.Product.apply)

  implicit val productEncoder: Encoder[domain.Product] =
    Encoder.forProduct3(ID, NAME, STOCK)(p => (p.id, p.name, p.stock))
}
