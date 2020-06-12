package com.leysoft.products.adapter.out.doobie.util

import cats.effect.{Async, ContextShift}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import fs2.Stream
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait DoobieUtil[P[_]] {

  def read[T](sqlStatement: Query0[T]): P[Option[T]]

  def readStreams[T](sqlStatement: Query0[T]): Stream[P, T]

  def readList[T](sqlStatement: Query0[T]): P[List[T]]

  def write(sqlStatement: Update0): P[Int]
}

final class HikariDoobieUtil[P[_]: Async: ContextShift] private (
  implicit transactor: HikariTransactor[P]
) extends DoobieUtil[P] {
  import cats.syntax.apply._

  private val logger =
    Slf4jLogger.getLoggerFromClass[P](HikariDoobieUtil.getClass)

  override def read[T](sqlStatement: Query0[T]): P[Option[T]] = {
    logger.info(s"READ: ${sqlStatement.sql}") *> sqlStatement.option.transact(
      transactor
    )
  }

  override def readStreams[T](sqlStatement: Query0[T]): Stream[P, T] =
    sqlStatement.stream.transact(transactor)

  override def readList[T](sqlStatement: Query0[T]): P[List[T]] = {
    logger.info(s"READ_LIST: ${sqlStatement.sql}") *> sqlStatement.stream.compile.toList
      .transact(transactor)
  }

  override def write(sqlStatement: Update0): P[Int] = {
    logger.info(s"WRITE: ${sqlStatement.sql}") *> sqlStatement.run.transact(
      transactor
    )
  }
}

object HikariDoobieUtil {

  def make[P[_]: Async: ContextShift](
    implicit transactor: HikariTransactor[P]
  ): P[HikariDoobieUtil[P]] =
    Async[P].delay(new HikariDoobieUtil[P])
}
