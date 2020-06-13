package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.products.adapter.auth.Auth.{AuthService, InMemoryUserRepository}
import com.leysoft.products.adapter.in.api.{LoginRoute, ProductRoute}
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.redis.RedisProductRepository
import com.leysoft.products.adapter.out.redis.config.RedisConfiguration
import com.leysoft.products.adapter.out.redis.util.DefaultRedisUtil
import com.leysoft.products.application.DefaultProductService
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS

object ApiRedisCats extends IOApp {
  import com.leysoft.products.adapter.config._
  import org.http4s.implicits._ // for orNotFound
  import cats.syntax.semigroupk._ // for <+>
  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    RedisConfiguration
      .redis[IO]
      .use { implicit commands =>
        for {
          conf <- config.load[IO]
          redisUtil <- DefaultRedisUtil.make[IO]
          repository <- RedisProductRepository.make[IO](redisUtil)
          service <- DefaultProductService.make[IO](repository)
          error <- ErrorHandler.make[IO]
          handler <- error.handler
          userRepository <- InMemoryUserRepository.make[IO]
          auth <- AuthService.make[IO](conf.auth, userRepository)
          middleware <- auth.middleware
          login <- LoginRoute.make[IO](auth)
          api <- ProductRoute.make[IO](service)
          _ <-
            BlazeServerBuilder[IO]
              .bindHttp(port = conf.api.port.value, host = conf.api.host.value)
              .withHttpApp(
                CORS {
                  (api.routes(middleware, handler) <+> login
                    .routes(handler)).orNotFound
                }
              )
              .serve
              .compile
              .drain
        } yield ExitCode.Success
      }
}
