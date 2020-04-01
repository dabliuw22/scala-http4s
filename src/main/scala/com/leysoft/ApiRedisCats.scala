package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.products.adapter.in.api.ProductRoute
import com.leysoft.products.adapter.in.api.error.ErrorHandler
import com.leysoft.products.adapter.out.redis.RedisProductRepository
import com.leysoft.products.adapter.out.redis.config.RedisConfiguration
import com.leysoft.products.adapter.out.redis.util.DefaultRedisUtil
import com.leysoft.products.application.DefaultProductService
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder

object ApiRedisCats extends IOApp {
  import org.http4s.implicits._
  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    RedisConfiguration
      .redis[IO]
      .use { commands =>
        for {
          redisUtil <- DefaultRedisUtil.make(commands)
          repository <- RedisProductRepository.make[IO](redisUtil)
          service <- DefaultProductService.make[IO](repository)
          api <- ProductRoute.make[IO](service)
          error <- ErrorHandler.make[IO]
          _ <- BlazeServerBuilder[IO]
                .bindHttp(port = 8080, host = "localhost")
                .withHttpApp(api.routes(error.handler).orNotFound)
                .serve
                .compile
                .drain
        } yield ExitCode.Success
      }
}
