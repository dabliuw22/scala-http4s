package com.leysoft.products.adapter.out.doobie

import cats.effect.Effect
import com.leysoft.products.adapter.out.doobie.util.Doobie
import com.leysoft.products.domain.{Product, ProductRepository}
import doobie.util.query.Query0
import doobie.util.update.Update0

final class DoobieProductRepository[P[_]: Effect: Doobie] private (
) extends ProductRepository[P] {
  import DoobieProductRepository._

  override def findBy(id: String): P[Option[Product]] =
    Doobie[P].option(getById(id))

  override def findAll: P[List[Product]] =
    Doobie[P].list(getAll)

  override def findAllAStreams: fs2.Stream[P, Product] =
    Doobie[P].stream(getAll)

  override def save(product: Product): P[Int] =
    Doobie[P].command(insert(product))

  override def update(product: Product): P[Int] =
    Doobie[P].command(upd(product))

  override def delete(id: String): P[Int] =
    Doobie[P].command(del(id))
}

object DoobieProductRepository {
  import doobie.implicits._
  import doobie.implicits.javatime._

  def make[P[_]: Effect: Doobie]: P[DoobieProductRepository[P]] =
    Effect[P].delay(new DoobieProductRepository[P])

  private def getById(id: String): Query0[Product] =
    sql"SELECT * FROM products WHERE id = $id".query[Product]

  private def getAll: Query0[Product] =
    sql"SELECT * FROM products".query[Product]

  private def insert(product: Product): Update0 =
    sql"""INSERT INTO products
          |VALUES(${product.id}, ${product.name},
          |${product.stock}, ${product.createdAt})""".stripMargin.update

  private def upd(product: Product): Update0 =
    sql"""UPDATE products
         |SET name = ${product.name}, stock = ${product.stock}
         |WHERE id = ${product.id}""".stripMargin.update

  private def del(id: String): Update0 =
    sql"DELETE FROM products WHERE id = $id".update
}
