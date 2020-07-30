package com.leysoft.products

import cats.effect.IO
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.leysoft.products.adapter.out.doobie.util.Doobie
import doobie.implicits._
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

  container.start

  private lazy val transactor: Transactor[IO] =
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = container.jdbcUrl,
      user = container.username,
      pass = container.password,
      blocker = blocker
    )

  protected implicit def util: Doobie[IO] =
    Doobie.make[IO](transactor).unsafeRunSync()

  protected def createTable(table: Update0): IO[Unit] =
    table.run.transact(transactor).void

  override protected def afterAll: Unit = container.stop
}
