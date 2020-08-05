package com.leysoft.products.adapter.out.doobie.config

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari._
import doobie.util.ExecutionContexts
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.string.NonEmptyString

final case class DoobieConfiguration[P[_]: Async: ContextShift]() {

  private val driver: NonEmptyString = "org.postgresql.Driver"

  private val url: NonEmptyString = "jdbc:postgresql://localhost:5432/http4s_db"

  private val user: NonEmptyString = "http4s"

  private val password: NonEmptyString = "http4s"

  private val threadSize: Int Refined Interval.Open[0, 32] = 10

  def transactor(block: Blocker): Resource[P, HikariTransactor[P]] =
    for {
      context <- ExecutionContexts.fixedThreadPool[P](threadSize)
      hikari <- HikariTransactor.newHikariTransactor[P](
                  driverClassName = driver,
                  url = url,
                  user = user,
                  pass = password,
                  connectEC = context,
                  blocker = block
                )
    } yield hikari
}
