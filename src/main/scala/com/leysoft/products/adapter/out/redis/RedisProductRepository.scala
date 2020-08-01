package com.leysoft.products.adapter.out.redis

import cats.effect.Effect
import com.leysoft.products.adapter.out.redis.util.{Decoder, Encoder, Redis}
import com.leysoft.products.domain.{Product, ProductRepository}

import scala.concurrent.duration._

final class RedisProductRepository[P[_]: Effect: Redis] private (
) extends ProductRepository[P] {
  import RedisProductRepository._
  import cats.syntax.apply._
  import cats.syntax.applicativeError._
  import cats.syntax.functor._

  override def findBy(id: String): P[Option[Product]] =
    Redis[P]
      .hmGet(id, idField, nameField, stockName)

  override def findAll: P[List[Product]] = Effect[P].delay(List())

  override def findAllAStreams: fs2.Stream[P, Product] =
    fs2.Stream.emits(List[Product]()).covary[P]

  override def save(product: Product): P[Int] =
    (Redis[P].hmSet(product.id, product) <*
      Redis[P].expire(product.id, expiration))
      .map(_ => 1)
      .handleError(_ => 0)

  override def update(product: Product): P[Int] = save(product)

  override def delete(id: String): P[Int] =
    Redis[P]
      .hDel(id, nameField, stockName)
      .map(_ => 1)
      .handleError(_ => 0)
}

object RedisProductRepository {

  private val idField = "id"

  private val nameField = "name"

  private val stockName = "stock"

  private val expiration = 10 minutes

  private implicit val decoder: Decoder[Product] = hash =>
    hash
      .get(idField)
      .flatMap(id =>
        hash
          .get(nameField)
          .flatMap(name =>
            hash
              .get(stockName)
              .map(stock => Product(id, name, stock.toDouble))
          )
      )

  private implicit val encoder: Encoder[Product] = product =>
    Map(
      idField -> product.id,
      nameField -> product.name,
      stockName -> product.stock.toString
    )

  def make[P[_]: Effect: Redis]: P[RedisProductRepository[P]] =
    Effect[P].delay(new RedisProductRepository)
}
