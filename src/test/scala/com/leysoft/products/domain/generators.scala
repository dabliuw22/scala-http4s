package com.leysoft.products.domain

import org.scalacheck.Gen

object generators {

  val genId: Gen[String] = Gen.uuid.map(_.toString)

  val genProduct: Gen[Product] = for {
    id <- genId
    name <- Gen.alphaStr
    stock <- Gen.choose(0, 100)
  } yield Product(id, name, stock)
}
