package com.leysoft.products

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.leysoft.products.adapter.out.doobie.util.Doobie
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
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

  implicit def util: Doobie[IO] =
    new Doobie[IO] {

      override def option[T](query: Query0[T]): IO[Option[T]] =
        query.option.transact(transactor)

      override def stream[T](
        query: Query0[T]
      ): fs2.Stream[IO, T] =
        query.stream.transact(transactor)

      override def list[T](query: Query0[T]): IO[List[T]] =
        query.stream.compile.toList.transact(transactor)

      override def command(command: Update0): IO[Int] =
        command.run.transact(transactor)
    }

  def createTable(sqlStatement: Update0): IO[Unit] =
    sqlStatement.run.transact(transactor).void

  override protected def beforeAll: Unit = container.start

  override protected def afterAll: Unit = container.stop
}
