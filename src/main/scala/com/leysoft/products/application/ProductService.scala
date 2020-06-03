package com.leysoft.products.application

import cats.effect.Effect
import com.leysoft.products.domain.error.{MonadThrow, ProductNotFoundException, ProductWritingException}
import com.leysoft.products.domain.{Product, ProductRepository}
import fs2.Stream

sealed trait ProductService[P[_]] {

  def get(id: String): P[Product]

  def getAll: P[List[Product]]

  def getAllStreams: Stream[P, Product]

  def create(product: Product): P[Product]

  def update(product: Product): P[Product]

  def remove(id: String): P[Boolean]
}

final class DefaultProductService[P[_]: Effect: MonadThrow](
  productRepository: ProductRepository[P]
) extends ProductService[P] {
  import cats.syntax.monadError._
  import cats.syntax.functor._

  override def get(id: String): P[Product] =
    productRepository
      .findBy(id)
      .map {
        case Some(value) => value
        case _ =>
          throw ProductNotFoundException(s"Not Found Product By Id: $id")
      }
      .adaptError {
        case e => ProductNotFoundException(e.getMessage)
      }

  override def getAll: P[List[Product]] =
    productRepository.findAll
      .adaptError(e => ProductNotFoundException(e.getMessage))

  override def getAllStreams: Stream[P, Product] =
    productRepository.findAllAStreams
      .adaptError(e => ProductNotFoundException(e.getMessage))

  override def create(product: Product): P[Product] =
    productRepository
      .save(product)
      .map {
        validate(
          _,
          s"It Was Not Possible To Create The Product With id: ${product.id}",
          product
        )
      }
      .adaptError(e => ProductWritingException(e.getMessage))

  override def update(product: Product): P[Product] =
    productRepository
      .update(product)
      .map {
        validate(
          _,
          s"It Was Not Possible To Update The Product With id: ${product.id}",
          product
        )
      }
      .adaptError(e => ProductWritingException(e.getMessage))

  override def remove(id: String): P[Boolean] =
    productRepository
      .delete(id)
      .map {
        case 0 => false
        case _ => true
      }
      .adaptError(e => ProductWritingException(e.getMessage))

  private def validate[A](count: Int, message: String, a: A): A = count match {
    case 0 => throw ProductWritingException(message)
    case _ => a
  }
}

object DefaultProductService {

  def make[P[_]: Effect](
    repository: ProductRepository[P]
  ): P[DefaultProductService[P]] =
    Effect[P].delay(new DefaultProductService[P](repository))
}
