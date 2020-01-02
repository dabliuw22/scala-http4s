package com.leysoft.products.domain

trait ProductRepository[P[_]] {

  def findBy(id: Long): P[Product]

  def findAll: P[List[Product]]
}
