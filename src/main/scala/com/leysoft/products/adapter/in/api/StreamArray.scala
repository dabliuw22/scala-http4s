package com.leysoft.products.adapter.in.api

import cats.effect.Effect
import fs2.Stream

case class StreamArray[P[_]: Effect, E](stream: Stream[P, E])

object StreamArray {

  def make[P[_]: Effect, E](stream: Stream[P, E]): P[StreamArray[P, E]] =
    Effect[P].delay(StreamArray(stream))
}
