package com.leysoft.products.domain

trait ProductRepository[P[_]] {

  def findBy(id: Long): P[Option[Product]]

  def findAll: P[List[Product]]

  def save(product: Product): P[Int]

  def update(product: Product): P[Int]

  def delete(id: Long): P[Int]
}
