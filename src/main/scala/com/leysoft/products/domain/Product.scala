package com.leysoft.products.domain

import java.time.OffsetDateTime
import java.util.UUID

case class Product(
  id: String = UUID.randomUUID().toString,
  name: String,
  stock: Double,
  createdAt: OffsetDateTime = OffsetDateTime.now()
)

object Product {

  def make(name: String, stock: Double): Product =
    new Product(name = name, stock = stock)
}
