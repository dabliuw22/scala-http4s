package com.leysoft.products.adapter.out.skunk

import cats.effect.Effect
import com.leysoft.products.domain.{Product, ProductRepository}
import fs2.Stream
import skunk.Session
import skunk.data.Completion

final class SkunkProductRepository[P[_]: Effect] private (implicit
  session: Session[P]
) extends ProductRepository[P] {
  import SkunkProductRepository._
  import cats.syntax.functor._
  import skunk.Void

  override def findBy(id: String): P[Option[Product]] =
    session
      .prepare(byId)
      .use(prepared => prepared.option(id))

  override def findAll: P[List[Product]] = session.execute(all)

  override def findAllAStreams: Stream[P, Product] =
    for {
      prepared <- Stream.resource(session.prepare(all))
      stream <- prepared.stream(Void, 64)
    } yield stream

  override def save(product: Product): P[Int] =
    session
      .prepare(ins)
      .use { prepared =>
        prepared
          .execute(product)
          .map { case Completion.Insert(count) => count }
      }

  override def update(product: Product): P[Int] =
    session
      .prepare(upd)
      .use { prepared =>
        prepared
          .execute((product.name, product.stock), product.id)
          .map {
            case Completion.Update(count) => count
            case Completion.Delete(count) => count
          }
      }

  override def delete(id: String): P[Int] =
    session
      .prepare(del)
      .use { prepared =>
        prepared
          .execute(id)
          .map { case Completion.Delete(count) => count }
      }
}

object SkunkProductRepository {
  import skunk._
  import skunk.implicits._
  import skunk.codec.all._

  def make[P[_]: Effect](implicit
    session: Session[P]
  ): P[SkunkProductRepository[P]] =
    Effect[P].delay(new SkunkProductRepository)

  private def all: Query[Void, Product] =
    sql"SELECT * FROM products"
      .query(varchar ~ varchar ~ float8 ~ timestamptz)
      .map {
        case id ~ name ~ stock ~ createdAt =>
          Product(id, name, stock, createdAt)
      }

  private def byId: Query[String, Product] =
    sql"SELECT * FROM products WHERE id = $varchar"
      .query(varchar ~ varchar ~ float8 ~ timestamptz)
      .map {
        case id ~ name ~ stock ~ createdAt =>
          Product(id, name, stock, createdAt)
      }

  private def ins: Command[Product] =
    sql"""INSERT INTO products(id, name, stock, created_at)
           VALUES($varchar, $varchar, $float8, $timestamptz)""".command
      .contramap {
        case Product(i, n, s, c) =>
          i ~ n ~ s ~ c
      }

  private def upd: Command[String ~ Double ~ String] =
    sql"""UPDATE products SET name = $varchar, stock = $float8
         WHERE id = $varchar""".command

  private def del: Command[String] =
    sql"DELETE FROM products WHERE id = $varchar".command
}
