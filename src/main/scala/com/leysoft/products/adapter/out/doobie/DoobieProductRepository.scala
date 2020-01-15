package com.leysoft.products.adapter.out.doobie

import cats.effect.Effect
import com.leysoft.products.adapter.out.doobie.util.DoobieUtil
import com.leysoft.products.domain.{Product, ProductRepository}

final case class DoobieProductRepository[P[_]: Effect]()(implicit doobieUtil: DoobieUtil[P]) extends ProductRepository[P] {
  import doobie.implicits._

  override def findBy(id: Long): P[Option[Product]] = doobieUtil
    .read(sql"SELECT * FROM products WHERE id = $id".query[Product])

  override def findAll: P[List[Product]] = doobieUtil
    .readList(sql"SELECT * FROM products".query[Product])

  override def save(product: Product): P[Int] = doobieUtil
    .write(sql"INSERT INTO products VALUES(${product.id}, ${product.name}, ${product.stock})".update)

  override def update(product: Product): P[Int] = doobieUtil
    .write(sql"""UPDATE products SET name = ${product.name}, stock = ${product.stock}
                 WHERE id = ${product.id}""".update)

  override def delete(id: Long): P[Int] = doobieUtil
    .write(sql"DELETE FROM products WHERE id = $id".update)
}
