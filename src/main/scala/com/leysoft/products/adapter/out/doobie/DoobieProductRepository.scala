package com.leysoft.products.adapter.out.doobie

import com.leysoft.products.adapter.out.doobie.util.DoobieUtil
import com.leysoft.products.domain.{ProductRepository, Product}

final case class DoobieProductRepository[P[_]]()(implicit doobieUtil: DoobieUtil[P]) extends ProductRepository[P] {
  import doobie.implicits._

  override def findBy(id: Long): P[Option[Product]] = doobieUtil
    .execute(sql"SELECT * FROM products WHERE id = $id".query[Product].option)

  override def findAll: P[List[Product]] = doobieUtil
    .execute(sql"SELECT * FROM products".query[Product].stream.compile.toList)
}
