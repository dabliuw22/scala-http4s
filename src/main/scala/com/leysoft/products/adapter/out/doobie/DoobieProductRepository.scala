package com.leysoft.products.adapter.out.doobie

import com.leysoft.products.adapter.out.doobie.util.DoobieUtil
import com.leysoft.products.domain
import com.leysoft.products.domain.ProductRepository

final case class DoobieProductRepository[P[_]]()(implicit doobieUtil: DoobieUtil[P]) extends ProductRepository[P] {
  import doobie.implicits._

  override def findBy(id: Long): P[domain.Product] = doobieUtil
    .execute(sql"SELECT * FROM products WHERE id = $id".query[domain.Product].unique)

  override def findAll: P[List[domain.Product]] = doobieUtil
    .execute(sql"SELECT * FROM products".query[domain.Product].stream.compile.toList)
}
