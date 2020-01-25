package com.leysoft.products.adapter.out.skunk

import cats.effect.Effect
import com.leysoft.products.domain
import com.leysoft.products.domain.ProductRepository
import skunk.Session
import skunk.data.Completion

final class SkunkProductRepository[P[_]: Effect] private(session: Session[P]) extends ProductRepository[P]  {
  import SkunkProductRepository._
  import cats.syntax.functor._

  override def findBy(id: String): P[Option[domain.Product]] = session.prepare(byId)
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

  override def delete(id: String): P[Int] = session.prepare(del)
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
    .query(varchar ~ varchar ~ float8)
    .map { case id ~ name ~ stock => domain.Product(id, name, stock) }

  def byId: Query[String, domain.Product] = sql"SELECT * FROM products WHERE id = $varchar"
    .query(varchar ~ varchar ~ float8)
    .map { case id ~ name ~ stock => domain.Product(id, name, stock) }

  def ins: Command[String ~ String ~ Double] =
    sql"INSERT INTO products VALUES($varchar, $varchar, $float8)".command

  def upd: Command[String ~ Double ~ String] =
    sql"""UPDATE products SET name = $varchar, stock = $float8
         WHERE id = $varchar""".command

  def del: Command[String] = sql"DELETE FROM products WHERE id = $varchar".command
}
