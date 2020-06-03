package com.leysoft.products.domain

import cats.MonadError

package object error {

  type MonadThrow[F[_]] = MonadError[F, Throwable]

  case class ProductNotFoundException(message: String)
      extends RuntimeException(message)

  case class ProductWritingException(message: String)
      extends RuntimeException(message)
}
