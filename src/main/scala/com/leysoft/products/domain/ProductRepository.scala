package com.leysoft.products.domain

trait ProductRepository[P[_]] {
  import fs2.Stream

  def findBy(id: String): P[Option[Product]]

  def findAll: P[List[Product]]

  def findAllAStreams: Stream[P, Product]

  def save(product: Product): P[Int]

  def update(product: Product): P[Int]

  def delete(id: String): P[Int]
}
