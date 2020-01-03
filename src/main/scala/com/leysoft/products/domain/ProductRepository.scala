package com.leysoft.products.domain

trait ProductRepository[P[_]] {

  def findBy(id: Long): P[Option[Product]]

  def findAll: P[List[Product]]
}
