package com.leysoft.products.adapter.out.redis.config

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import dev.profunktor.redis4cats.algebra.RedisCommands
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.domain.RedisCodec
import dev.profunktor.redis4cats.interpreter.Redis
import io.chrisdavenport.log4cats.Logger

object RedisConfiguration {

  import dev.profunktor.redis4cats.log4cats._

  private val uri: String = "redis://localhost:6379"

  def redis[P[_]: ConcurrentEffect: ContextShift: Logger]
    : Resource[P, RedisCommands[P, String, String]] =
    for {
      uri <- Resource.liftF(RedisURI.make[P](uri))
      client <- RedisClient[P](uri)
      cmd <- Redis[P, String, String](client, RedisCodec.Utf8)
    } yield cmd
}
