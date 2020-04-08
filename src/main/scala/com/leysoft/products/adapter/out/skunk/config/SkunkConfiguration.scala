package com.leysoft.products.adapter.out.skunk.config

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.{Interval, Positive}
import natchez.Trace
import skunk.{Session, SessionPool}

case class SkunkConfiguration[P[_]: ConcurrentEffect: ContextShift: Trace]() {

  private val host: String Refined NonEmpty = "localhost"

  private val port: Int Refined Positive = 5432

  private val user: String Refined NonEmpty = "http4s"

  private val password: String Refined NonEmpty = "http4s"

  private val database: String Refined NonEmpty = "http4s_db"

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
