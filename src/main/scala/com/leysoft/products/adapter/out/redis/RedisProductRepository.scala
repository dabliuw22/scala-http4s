package com.leysoft.products.adapter.out.redis

import cats.effect.Effect
import com.leysoft.products.adapter.out.redis.util.RedisUtil
import com.leysoft.products.domain
import com.leysoft.products.domain.ProductRepository

import scala.concurrent.duration._

final class RedisProductRepository[P[_]: Effect] private (
  redisUtil: RedisUtil[P]
) extends ProductRepository[P] {
  import RedisProductRepository._
  import cats.syntax.applicativeError._
  import cats.syntax.functor._

  override def findBy(id: String): P[Option[domain.Product]] =
    redisUtil
      .hmGet(id, fa, idField, nameField, stockName)

  override def findAll: P[List[domain.Product]] = Effect[P].delay(List())

  override def findAllAStreams: fs2.Stream[P, domain.Product] =
    fs2.Stream.emits(List[domain.Product]()).covary[P]

  override def save(product: domain.Product): P[Int] =
    redisUtil
      .hmSet(product.id, fb(product), expiration)
      .map(_ => 1)
      .handleError(_ => 0)

  override def update(product: domain.Product): P[Int] = save(product)

  override def delete(id: String): P[Int] =
    redisUtil
      .hDel(id, nameField, stockName)
      .map(_ => 1)
      .handleError(_ => 0)
}

object RedisProductRepository {

  private val idField = "id"

  private val nameField = "name"

  private val stockName = "stock"

  private val expiration = 10 minutes

  private val fa: Map[String, String] => Option[domain.Product] = hash =>
    hash
      .get(idField)
      .flatMap(id =>
        hash
          .get(nameField)
          .flatMap(name =>
            hash
              .get(stockName)
              .map(stock => domain.Product(id, name, stock.toDouble))
          )
      )

  private val fb: domain.Product => Map[String, String] = product =>
    Map(
      idField -> product.id,
      nameField -> product.name,
      stockName -> product.stock.toString
    )

  def make[P[_]: Effect](
    commands: RedisUtil[P]
  ): P[RedisProductRepository[P]] =
    Effect[P].delay(new RedisProductRepository(commands))
}
