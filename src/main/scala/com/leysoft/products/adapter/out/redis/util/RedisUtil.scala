package com.leysoft.products.adapter.out.redis.util

import cats.effect.Effect
import dev.profunktor.redis4cats.algebra.RedisCommands

import scala.concurrent.duration.FiniteDuration

sealed trait RedisUtil[P[_]] {

  def hmGet[A](key: String, fa: Map[String, String] => A, fields: String*): P[A]

  def hmSet[A](key: String,
               fieldValues: Map[String, String],
               expiration: FiniteDuration): P[Unit]

  def hDel(key: String, fields: String*): P[Unit]
}

final class DefaultRedisUtil[P[_]: Effect] private (
  implicit commands: RedisCommands[P, String, String]
) extends RedisUtil[P] {
  import cats.syntax.apply._
  import cats.syntax.functor._

  override def hmGet[A](key: String,
                        fa: Map[String, String] => A,
                        fields: String*): P[A] =
    commands
      .hmGet(key, fields.distinct: _*)
      .map(fa)

  override def hmSet[A](key: String,
                        fieldValues: Map[String, String],
                        expiration: FiniteDuration): P[Unit] =
    commands
      .hmSet(key, fieldValues) *> commands
      .expire(key, expiration)

  override def hDel(key: String, fields: String*): P[Unit] =
    commands
      .hDel(key, fields.distinct: _*)
}

object DefaultRedisUtil {

  def make[P[_]: Effect](
    implicit commands: RedisCommands[P, String, String]
  ): P[RedisUtil[P]] =
    Effect[P].delay(new DefaultRedisUtil)
}
