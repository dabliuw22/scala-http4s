package com.leysoft.products.adapter.out.doobie.util

import cats.effect.{Async, ContextShift}
import com.leysoft.products.adapter.out.doobie.config.DoobieConfiguration
import com.typesafe.scalalogging.Logger
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import org.slf4j.LoggerFactory

trait DoobieUtil[P[_]] {

  def read[T](sqlStatement: Query0[T]): P[Option[T]]

  def readList[T](sqlStatement: Query0[T]): P[List[T]]

  def write(sqlStatement: Update0): P[Int]
}

final case class HikariDoobieUtil[P[_]: Async: ContextShift]()(implicit db: DoobieConfiguration[P]) extends DoobieUtil[P] {

  private val resource = db.hikariTransactor

  private val looger = Logger(LoggerFactory.getLogger(HikariDoobieUtil.getClass))

  def read[T](sqlStatement: Query0[T]): P[Option[T]] = {
    looger.info(s"READ: ${sqlStatement.sql}")
    resource.use { hikari => sqlStatement.option.transact(hikari) }
  }

  def readList[T](sqlStatement: Query0[T]): P[List[T]] = {
    looger.info(s"READ_LIST: ${sqlStatement.sql}")
    resource.use { hikari => sqlStatement.stream.compile.toList.transact(hikari) }
  }

  def write(sqlStatement: Update0): P[Int] = {
    looger.info(s"WRITE: ${sqlStatement.sql}")
    resource.use { hikari => sqlStatement.run.transact(hikari) }
  }
}
