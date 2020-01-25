package com.leysoft.products.adapter.out.skunk

import cats.effect.Effect
import com.leysoft.products.domain
import com.leysoft.products.domain.ProductRepository
import skunk.Session
import skunk.data.Completion

final class SkunkProductRepository[P[_]: Effect] private(session: Session[P]) extends ProductRepository[P]  {
  import SkunkProductRepository._
  import cats.syntax.functor._

  override def findBy(id: Int): P[Option[domain.Product]] = session.prepare(byId)
    .use(prepared => prepared.option(id))

  override def findAll: P[List[domain.Product]] = session.execute(all)

  override def save(product: domain.Product): P[Int] = session.prepare(ins)
    .use { prepared => prepared.execute((product.id, product.name), product.stock)
      .map { case Completion.Insert(count) => count }
    }

  override def update(product: domain.Product): P[Int] = session.prepare(upd)
    .use { prepared => prepared.execute((product.name, product.stock), product.id)
      .map {
        case Completion.Update(count) => count
        case Completion.Delete(count) => count
      }
    }

  override def delete(id: Int): P[Int] = session.prepare(del)
    .use { prepared => prepared.execute(id)
      .map { case Completion.Delete(count) => count }
    }
}

object SkunkProductRepository {
  import skunk._
  import skunk.implicits._
  import skunk.codec.all._

  private def apply[P[_]: Effect](session: Session[P]): SkunkProductRepository[P] = new SkunkProductRepository(session)

  def make[P[_]: Effect](session: Session[P]): P[SkunkProductRepository[P]] =
    Effect[P].delay(SkunkProductRepository(session))

  def all: Query[Void, domain.Product] = sql"SELECT * FROM products"
    .query(int4 ~ varchar ~ float8)
    .map { case id ~ name ~ stock => domain.Product(id, name, stock) }

  def byId: Query[Int, domain.Product] = sql"SELECT * FROM products WHERE id = $int4"
    .query(int4 ~ varchar ~ float8)
    .map { case id ~ name ~ stock => domain.Product(id, name, stock) }

  def ins: Command[Int ~ String ~ Double] =
    sql"INSERT INTO products VALUES($int4, $varchar, $float8)".command

  def upd: Command[String ~ Double ~ Int] =
    sql"""UPDATE products SET name = $varchar, stock = $float8
         WHERE id = $int4""".command

  def del: Command[Int] = sql"DELETE FROM products WHERE id = $int4".command
}
