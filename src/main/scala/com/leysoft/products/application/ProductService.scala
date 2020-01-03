package com.leysoft.products.application

import cats.Monad
import com.leysoft.products.domain.error.ProductNotFoundException
import com.leysoft.products.domain.{Product, ProductRepository}

trait ProductService[P[_]] {

  def get(id: Long): P[Product]

  def getAll: P[List[Product]]
}

final case class DefaultProductService[P[_]: Monad](productRepository: ProductRepository[P]) extends ProductService[P] {
  import cats.syntax.functor._

  override def get(id: Long): P[Product] = productRepository.findBy(id).map {
    case Some(value) => value
    case _ => throw ProductNotFoundException(s"Not Found Product By Id: $id")
  }

  override def getAll: P[List[Product]] = productRepository.findAll
}
