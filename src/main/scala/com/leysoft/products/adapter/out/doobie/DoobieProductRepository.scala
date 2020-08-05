package com.leysoft.products.adapter.out.doobie

import cats.effect.Effect
import com.leysoft.products.adapter.out.doobie.util.Doobie
import com.leysoft.products.domain.{Product, ProductRepository}

final class DoobieProductRepository[P[_]: Effect: Doobie] private (
) extends ProductRepository[P] {
  import doobie.implicits._

  override def findBy(id: String): P[Option[Product]] =
    Doobie[P]
      .option(sql"SELECT * FROM products WHERE id = $id".query[Product])

  override def findAll: P[List[Product]] =
    Doobie[P]
      .list(sql"SELECT * FROM products".query[Product])

  override def findAllAStreams: fs2.Stream[P, Product] =
    Doobie[P]
      .stream(sql"SELECT * FROM products".query[Product])

  override def save(product: Product): P[Int] =
    Doobie[P]
      .command(
        sql"""INSERT INTO products
             |VALUES(${product.id}, ${product.name}, ${product.stock})""".stripMargin.update
      )

  override def update(product: Product): P[Int] =
    Doobie[P]
      .command(
        sql"""UPDATE products
             |SET name = ${product.name}, stock = ${product.stock}
             |WHERE id = ${product.id}""".stripMargin.update
      )

  override def delete(id: String): P[Int] =
    Doobie[P]
      .command(sql"DELETE FROM products WHERE id = $id".update)
}

object DoobieProductRepository {

  def make[P[_]: Effect: Doobie]: P[DoobieProductRepository[P]] =
    Effect[P].delay(new DoobieProductRepository[P])
}
