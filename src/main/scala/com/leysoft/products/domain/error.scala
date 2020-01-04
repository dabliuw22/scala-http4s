package com.leysoft.products.domain

package object error {

  case class ProductNotFoundException(message: String) extends RuntimeException(message)

  case class ProductWritingException(message: String) extends RuntimeException(message)
}
