package com.leysoft.products.application

import cats.Monad
import cats.effect.Effect
import com.leysoft.products.domain.error.{ProductNotFoundException, ProductWritingException}
import com.leysoft.products.domain.{Product, ProductRepository}

trait ProductService[P[_]] {

  def get(id: String): P[Product]

  def getAll: P[List[Product]]

  def create(product: Product): P[Product]

  def update(product: Product): P[Product]

  def remove(id: String): P[Boolean]
}

final class DefaultProductService[P[_]: Effect: Monad] private (productRepository: ProductRepository[P]) extends ProductService[P] {
  import cats.syntax.applicativeError._
  import cats.syntax.functor._

  override def get(id: String): P[Product] = productRepository.findBy(id).map {
    case Some(value) => value
    case _ => throw ProductNotFoundException(s"Not Found Product By Id: $id")
  }.handleError(e => throw ProductNotFoundException(e.getMessage))

  override def getAll: P[List[Product]] = productRepository.findAll
    .handleError(e => throw ProductNotFoundException(e.getMessage))

  override def create(product: Product): P[Product] = productRepository.save(product).map {
    case 0 => throw ProductWritingException(s"It Was Not Possible To Create The Product With id: ${product.id}")
    case _ => product
  }.handleError(e => throw ProductWritingException(e.getMessage))

  override def update(product: Product): P[Product] = productRepository.update(product).map {
    case 0 => throw ProductWritingException(s"It Was Not Possible To Update The Product With id: ${product.id}")
    case _ => product
  }.handleError(e => throw ProductWritingException(e.getMessage))

  override def remove(id: String): P[Boolean] = productRepository.delete(id).map {
    case 0 => false
    case _ => true
  }.handleError(e => throw ProductWritingException(e.getMessage))
}

object DefaultProductService {

  private def apply[P[_]: Effect](productRepository: ProductRepository[P]): DefaultProductService[P] = new DefaultProductService(productRepository)

  def make[P[_]: Effect](repository: ProductRepository[P]): P[DefaultProductService[P]] =
    Effect[P].delay(DefaultProductService[P](repository))
}
