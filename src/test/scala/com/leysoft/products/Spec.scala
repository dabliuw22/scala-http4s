package com.leysoft.products

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait Spec
    extends AnyWordSpec
    with Matchers
    with MockFactory
    with BeforeAndAfterEach {}
