package com.leysoft.products.adapter.out.doobie

import java.util.UUID

import cats.effect.IO
import com.leysoft.products.PostgresItSpec
import com.leysoft.products.domain.{Product, ProductRepository}
import doobie.implicits._

final class DoobieProductRepositoryItSpec extends PostgresItSpec {

  val product: Product = Product("test_product", 10)

  val repository: IO[ProductRepository[IO]] = DoobieProductRepository.make[IO](util)

  "DoobieProductRepository.findBy" should {
    "Return One Record" in {
      val effect = for {
        repo <- repository
        _ <- repo.save(product)
        result <- repo.findBy(product.id)
        status = result == Option(product)
      } yield assert(status)
      effect.unsafeToFuture
    }
  }

  override protected def beforeEach(): Unit = {
    container.start
    createTable(
      sql"""CREATE TABLE products (
           |    id VARCHAR PRIMARY KEY,
           |    name VARCHAR NOT NULL,
           |    stock FLOAT8 NOT NULL
           |)
           |""".stripMargin.update
    ).unsafeRunSync
  }

  override protected def afterEach(): Unit = container.stop
}
