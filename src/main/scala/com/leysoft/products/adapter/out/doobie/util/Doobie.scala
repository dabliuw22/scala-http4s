package com.leysoft.products.adapter.out.doobie.util

import cats.effect.{Async, ContextShift, Sync}
import cats.syntax.apply._
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
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

  def make[P[_]: Sync](
    transactor: Transactor[P]
  ): P[Doobie[P]] =
    Sync[P].delay(doobie(transactor))

  private def doobie[P[_]: Sync](
    transactor: Transactor[P]
  ): Doobie[P] =
    new Doobie[P] {

      private val logger =
        Slf4jLogger.getLoggerFromClass[P](this.getClass)

      override def option[T](query: Query0[T]): P[Option[T]] =
        logger.info(s"Option: ${query.sql}") *>
          query.option.transact(transactor)

      override def stream[T](query: Query0[T]): Stream[P, T] =
        Stream.eval(logger.info(s"Stream: ${query.sql}")) >>
          query.stream.transact(transactor)

      override def list[T](query: Query0[T]): P[List[T]] =
        logger.info(s"List: ${query.sql}") *>
          query.stream.compile.toList.transact(transactor)

      override def command(command: Update0): P[Int] =
        logger.info(s"Command: ${command.sql}") *>
          command.run.transact(transactor)
    }
}
