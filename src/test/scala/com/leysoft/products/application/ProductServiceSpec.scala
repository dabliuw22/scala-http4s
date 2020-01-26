package com.leysoft.products.application

import cats.effect.IO
import com.leysoft.products.{PropertieSpec, domain}
import com.leysoft.products.domain.{ProductRepository}

final class ProductServiceSpec extends PropertieSpec {
  import domain.arbitraries._

  def productRepository(product: domain.Product): TestProductRepository =
    new TestProductRepository {
      override def findBy(id: String): IO[Option[domain.Product]] = IO.pure(Some(product))
    }

  forAll { product: domain.Product =>
    spec("Succeful getById") {
      new DefaultProductService[IO](productRepository(product))
        .get(product.id)
        .map(p => assert(p.eq(product)))
    }
  }
}

protected class TestProductRepository extends ProductRepository[IO] {

  override def findBy(id: String): IO[Option[domain.Product]] = ???

  override def findAll: IO[List[domain.Product]] = ???

  override def save(product: domain.Product): IO[Int] = ???

  override def update(product: domain.Product): IO[Int] = ???

  override def delete(id: String): IO[Int] = ???
}
