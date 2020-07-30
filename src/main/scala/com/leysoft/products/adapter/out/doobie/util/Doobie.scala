package com.leysoft.products.adapter.out.doobie.util

import cats.effect.{Async, ContextShift}
import cats.syntax.apply._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import fs2.Stream
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import simulacrum.typeclass

@typeclass
trait Doobie[P[_]] {
  def option[T](query: Query0[T]): P[Option[T]]

  def stream[T](query: Query0[T]): Stream[P, T]

  def list[T](query: Query0[T]): P[List[T]]

  def command(command: Update0): P[Int]
}

object Doobie {

  def make[P[_]: Async: ContextShift](implicit
    transactor: HikariTransactor[P]
  ): P[Doobie[P]] =
    Async[P].delay(doobie(transactor))

  private def doobie[P[_]: Async: ContextShift](
    transactor: HikariTransactor[P]
  ): Doobie[P] =
    new Doobie[P] {

      private val logger =
        Slf4jLogger.getLoggerFromClass[P](this.getClass)

      override def option[T](query: Query0[T]): P[Option[T]] =
        logger.info(s"OPTION: ${query.sql}") *>
          query.option.transact(transactor)

      override def stream[T](query: Query0[T]): Stream[P, T] =
        Stream.eval(logger.info(s"STREAM: ${query.sql}")) >>
          query.stream.transact(transactor)

      override def list[T](query: Query0[T]): P[List[T]] =
        logger.info(s"LIST: ${query.sql}") *>
          query.stream.compile.toList.transact(transactor)

      override def command(command: Update0): P[Int] =
        logger.info(s"WRITE: ${command.sql}") *>
          command.run.transact(transactor)
    }
}
