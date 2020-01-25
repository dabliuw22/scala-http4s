package com.leysoft.products.adapter.out.doobie.util

import cats.effect.{Async, ContextShift}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait DoobieUtil[P[_]] {

  def read[T](sqlStatement: Query0[T]): P[Option[T]]

  def readList[T](sqlStatement: Query0[T]): P[List[T]]

  def write(sqlStatement: Update0): P[Int]
}

final case class HikariDoobieUtil[P[_]: Async: ContextShift] private (transactor: HikariTransactor[P]) extends DoobieUtil[P] {
  import cats.syntax.apply._

  private val logger = Slf4jLogger.getLoggerFromClass[P](HikariDoobieUtil.getClass)

  def read[T](sqlStatement: Query0[T]): P[Option[T]] = {
    logger.info(s"READ: ${sqlStatement.sql}") *> sqlStatement.option.transact(transactor)
  }

  def readList[T](sqlStatement: Query0[T]): P[List[T]] = {
    logger.info(s"READ_LIST: ${sqlStatement.sql}") *> sqlStatement.stream.compile.toList.transact(transactor)
  }

  def write(sqlStatement: Update0): P[Int] = {
    logger.info(s"WRITE: ${sqlStatement.sql}") *> sqlStatement.run.transact(transactor)
  }
}

object HikariDoobieUtil {

  private def apply[P[_]: Async: ContextShift](transactor: HikariTransactor[P]): HikariDoobieUtil[P] = new HikariDoobieUtil(transactor)

  def make[P[_]: Async: ContextShift](transactor: HikariTransactor[P]): P[HikariDoobieUtil[P]] =
    Async[P].delay(HikariDoobieUtil[P](transactor))
}
