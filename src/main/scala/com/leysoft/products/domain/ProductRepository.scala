package com.leysoft.products.domain

trait ProductRepository[P[_]] {

  def findBy(id: Int): P[Option[Product]]

  def findAll: P[List[Product]]

  def save(product: Product): P[Int]

  def update(product: Product): P[Int]

  def delete(id: Int): P[Int]
}
