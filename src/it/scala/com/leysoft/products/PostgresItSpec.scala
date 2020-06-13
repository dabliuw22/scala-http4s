package com.leysoft.products

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.leysoft.products.adapter.out.doobie.util.DoobieUtil
import doobie.implicits._
import doobie.util.{query, update}
import doobie.util.transactor.Transactor

abstract class PostgresItSpec extends ContainerItSpec {

  override val container: PostgreSQLContainer = PostgreSQLContainer
    .Def(
      dockerImageName = "postgres:11.1",
      databaseName = "test_db",
      username = "test",
      password = "test"
    )
    .createContainer

  private lazy val transactor: Transactor[IO] =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = container.jdbcUrl,
      user = container.username,
      pass = container.password,
      blocker = blocker
    )

  def util: DoobieUtil[IO] =
    new DoobieUtil[IO] {

      override def read[T](sqlStatement: query.Query0[T]): IO[Option[T]] =
        sqlStatement.option.transact(transactor)

      override def readStreams[T](
        sqlStatement: query.Query0[T]
      ): fs2.Stream[IO, T] =
        sqlStatement.stream.transact(transactor)

      override def readList[T](sqlStatement: query.Query0[T]): IO[List[T]] =
        sqlStatement.stream.compile.toList.transact(transactor)

      override def write(sqlStatement: update.Update0): IO[Int] =
        sqlStatement.run.transact(transactor)
    }

  def createTable(sqlStatement: update.Update0): IO[Unit] =
    sqlStatement.run.transact(transactor).void

  override protected def beforeAll: Unit = container.start

  override protected def afterAll: Unit = container.stop
}
