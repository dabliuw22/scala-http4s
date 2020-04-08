package com.leysoft.products.adapter.out.doobie.config

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari._
import doobie.util.ExecutionContexts
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Interval

case class DoobieConfiguration[P[_]: Async: ContextShift]() {

  private val driver: String Refined NonEmpty = "org.postgresql.Driver"

  private val url: String Refined NonEmpty = "jdbc:postgresql://localhost:5432/http4s_db"

  private val user: String Refined NonEmpty = "http4s"

  private val password: String Refined NonEmpty = "http4s"

  private val threadSize: Int Refined Interval.Open[0, 32] = 10

  lazy val transactor: Resource[P, HikariTransactor[P]] = for {
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
