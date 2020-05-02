package com.leysoft.products.adapter.in.api.error

import io.circe.{Encoder, Json}

object Codecs {

  implicit val productEncoder: Encoder[ErrorResponse] =
    Encoder.instance[ErrorResponse] { error =>
      Json.obj(
        ("message", Json.fromString(error.message))
      )
    }
}
