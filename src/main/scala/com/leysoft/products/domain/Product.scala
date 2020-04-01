package com.leysoft.products.domain

import java.util.UUID

case class Product(id: String = UUID.randomUUID().toString,
                   name: String,
                   stock: Double)

object Product {

  def apply(name: String, stock: Double): Product =
    new Product(name = name, stock = stock)
}
