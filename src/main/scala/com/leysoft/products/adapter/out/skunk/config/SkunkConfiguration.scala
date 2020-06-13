package com.leysoft.products.adapter.out.skunk.config

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.{Interval, Positive}
import eu.timepit.refined.types.string.NonEmptyString
import natchez.Trace
import skunk.{Session, SessionPool}

final case class SkunkConfiguration[
  P[_]: ConcurrentEffect: ContextShift: Trace
]() {

  private val host: NonEmptyString = "localhost"

  private val port: Int Refined Positive = 5432

  private val user: NonEmptyString = "http4s"

  private val password: NonEmptyString = "http4s"

  private val database: NonEmptyString = "http4s_db"

  private val threadSize: Int Refined Interval.Open[0, 32] = 10

  lazy val session: SessionPool[P] = Session.pooled[P](
    host = host,
    port = port,
    user = user,
    password = Some(password),
    database = database,
    max = threadSize
  )

  lazy val single: Resource[P, Session[P]] = Session.single[P](
    host = host,
    port = port,
    user = user,
    password = Some(password),
    database = database
  )

}
