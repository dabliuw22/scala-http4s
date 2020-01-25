package com.leysoft.products.adapter.out.skunk.config

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import natchez.Trace
import skunk.{Session, SessionPool}

case class SkunkConfiguration[P[_]: ConcurrentEffect: ContextShift: Trace]() {

  private val host = "localhost"

  private val port = 5432

  private val user = "http4s"

  private val password = Some("http4s")

  private val database = "http4s_db"

  private val threadSize = 10

  lazy val session: SessionPool[P] = Session.pooled[P](
    host = host,
    port = port,
    user = user,
    password = password,
    database = database,
    max = threadSize
  )

  lazy val singleSession: Resource[P, Session[P]] = Session.single[P](
    host = host,
    port = port,
    user = user,
    password = password,
    database = database
  )

}
