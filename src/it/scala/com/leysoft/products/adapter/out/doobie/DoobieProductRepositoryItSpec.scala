package com.leysoft.products.adapter.out.doobie

import java.util.UUID

import cats.effect.IO
import com.leysoft.products.PostgresItSpec
import com.leysoft.products.domain.{Product, ProductRepository}
import doobie.implicits._

final class DoobieProductRepositoryItSpec extends PostgresItSpec {

  val product: Product = Product.make("test_product", 10)

  val repository: IO[ProductRepository[IO]] = DoobieProductRepository.make[IO]

  "DoobieProductRepository.findBy" should {
    "Return One Record" in {
      val effect = for {
        repo <- repository
        _ <- repo.save(product)
        result <- repo.findBy(product.id)
        status = result
                   .map(r => (r.id, r.name, r.stock))
                   .contains((product.id, product.name, product.stock))
      } yield assert(status)
      effect.unsafeToFuture
    }
  }

  "DoobieProductRepository.findBy" should {
    "Return None Record" in {
      val effect = for {
        repo <- repository
        result <- repo.findBy(UUID.randomUUID.toString)
        status = result.isEmpty
      } yield assert(status)
      effect.unsafeToFuture
    }
  }

  "DoobieProductRepository.findAll" should {
    "Return One Record" in {
      val effect = for {
        repo <- repository
        result <- repo.findAll
        status = result.size == 1
      } yield assert(status)
      effect.unsafeToFuture
    }
  }

  override protected def beforeAll: Unit = {
    run(
      sql"""CREATE TABLE products (
           |    id VARCHAR PRIMARY KEY,
           |    name VARCHAR NOT NULL,
           |    stock FLOAT8 NOT NULL,
           |    created_at TIMESTAMP WITH TIME ZONE NOT NULL
           |)
           |""".stripMargin.update
    )
  }
}
