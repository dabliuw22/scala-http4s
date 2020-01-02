package com.leysoft.products.adapter.out.doobie.util

import cats.effect.{Async, ContextShift}
import com.leysoft.products.adapter.out.doobie.config.DoobieConfiguration
import doobie.implicits._
import doobie.free.connection.ConnectionIO

trait DoobieUtil[P[_]] {

  def execute[T](sqlStatement: ConnectionIO[T]): P[T]
}

final case class HikariDoobieUtil[P[_]: Async: ContextShift]()(implicit db: DoobieConfiguration[P]) extends DoobieUtil[P] {

  private val resource = db.hikariTransactor

  def execute[T](sqlStatement: ConnectionIO[T]): P[T] = resource.use { hikari =>  sqlStatement.transact(hikari) }
}
