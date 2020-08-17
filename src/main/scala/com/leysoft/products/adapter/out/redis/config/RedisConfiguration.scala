package com.leysoft.products.adapter.out.redis.config

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.Redis
import io.chrisdavenport.log4cats.Logger

object RedisConfiguration {

  import dev.profunktor.redis4cats.log4cats._

  private val uri: String = "redis://localhost:6379"

  val codec: RedisCodec[String, String] = RedisCodec.Utf8

  def redis[P[_]: ConcurrentEffect: ContextShift: Logger]
    : Resource[P, RedisCommands[P, String, String]] =
    for {
      uriRedis <- Resource.liftF(RedisURI.make[P](uri))
      client <- RedisClient[P].fromUri(uriRedis)
      cmd <- Redis[P].fromClient(client, codec)
    } yield cmd
}
