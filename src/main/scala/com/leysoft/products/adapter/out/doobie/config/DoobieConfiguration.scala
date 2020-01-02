package com.leysoft.products.adapter.out.doobie.config

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari._
import doobie.util.ExecutionContexts

case class DoobieConfiguration[P[_]: Async]()(implicit cf: ContextShift[P]) {

  private val driver = "org.postgresql.Driver"

  private val url = "jdbc:postgresql://localhost:5432/http4s_db"

  private val user = "http4s"

  private val password = "http4s"

  private val threadSize = 10

  lazy val hikariTransactor: Resource[P, HikariTransactor[P]] = for {
    context <- ExecutionContexts.fixedThreadPool[P](threadSize)
    blocker <- Blocker[P]
    hikari <- HikariTransactor.newHikariTransactor[P](
      driverClassName = driver,
      url = url,
      user = user,
      pass = password,
      connectEC = context,
      blocker = blocker
    )
  } yield hikari
}
