package com.leysoft.products.domain

import org.scalacheck.Arbitrary

object arbitraries {
  import generators._

  implicit val productArbitrary: Arbitrary[Product] = Arbitrary(genProduct)
}
