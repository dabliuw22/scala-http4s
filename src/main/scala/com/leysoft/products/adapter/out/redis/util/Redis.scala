package com.leysoft.products.adapter.out.redis.util

import cats.effect.Effect
import cats.syntax.apply._
import cats.syntax.functor._
import dev.profunktor.redis4cats.algebra.RedisCommands
import simulacrum.typeclass

import scala.concurrent.duration.FiniteDuration

@typeclass
trait Redis[P[_]] {
  def hmGet[A](key: String, fields: String*)(implicit
    decoder: Decoder[A]
  ): P[Option[A]]

  def hmSet[A](key: String, value: A, expiration: FiniteDuration)(implicit
    encoder: Encoder[A]
  ): P[Unit]

  def hDel(key: String, fields: String*): P[Unit]
}

object Redis {

  def make[P[_]: Effect](
    commands: RedisCommands[P, String, String]
  ): P[Redis[P]] =
    Effect[P].delay(redis(commands))

  private def redis[P[_]: Effect](
    commands: RedisCommands[P, String, String]
  ): Redis[P] =
    new Redis[P] {
      override def hmGet[A](key: String, fields: String*)(implicit
        decoder: Decoder[A]
      ): P[Option[A]] =
        commands
          .hmGet(key, fields.distinct: _*)
          .map(decoder.decode)

      override def hmSet[A](key: String, value: A, expiration: FiniteDuration)(
        implicit encoder: Encoder[A]
      ): P[Unit] =
        commands
          .hmSet(key, encoder.encode(value)) *> commands
          .expire(key, expiration)

      override def hDel(key: String, fields: String*): P[Unit] =
        commands
          .hDel(key, fields.distinct: _*)
    }
}

trait Decoder[A] {
  def decode(row: Map[String, String]): Option[A]
}

object Decoder {
  final def instance[A](fa: Map[String, String] => Option[A]): Decoder[A] =
    (row: Map[String, String]) => fa(row)
}

trait Encoder[A] {
  def encode(value: A): Map[String, String]
}

object Encoder {
  final def instance[A](fa: A => Map[String, String]): Encoder[A] =
    (value: A) => fa(value)
}
