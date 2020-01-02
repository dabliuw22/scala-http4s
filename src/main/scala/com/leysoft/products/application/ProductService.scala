package com.leysoft.products.application

import com.leysoft.products.domain.{Product, ProductRepository}

trait ProductService[P[_]] {

  def get(id: Long): P[Product]

  def getAll: P[List[Product]]
}

final case class DefaultProductService[P[_]](productRepository: ProductRepository[P]) extends ProductService[P] {

  override def get(id: Long): P[Product] = productRepository.findBy(id)

  override def getAll: P[List[Product]] = productRepository.findAll
}
