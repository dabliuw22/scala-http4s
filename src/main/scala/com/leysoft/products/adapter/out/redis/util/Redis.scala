package com.leysoft.products.adapter.out.redis.util

import cats.effect.Effect
import cats.syntax.functor._
import cats.syntax.apply._
import dev.profunktor.redis4cats.RedisCommands
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import simulacrum.typeclass

import scala.concurrent.duration.FiniteDuration

@typeclass
trait Redis[P[_]] {
  def hmGet[A](key: String, fields: String*)(implicit
    decoder: Decoder[A]
  ): P[Option[A]]

  def hmSet[A](key: String, value: A)(implicit
    encoder: Encoder[A]
  ): P[Unit]

  def hGet(key: String, field: String): P[Option[String]]

  def hSet(key: String, field: String, value: String): P[Unit]

  def hDel(key: String, fields: String*): P[Unit]

  def expire(key: String, expiration: FiniteDuration): P[Unit]
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

      private val logger =
        Slf4jLogger.getLoggerFromClass[P](this.getClass)

      override def hmGet[A](key: String, fields: String*)(implicit
        decoder: Decoder[A]
      ): P[Option[A]] =
        logger.info(s"hmGet: $key") *>
          commands
            .hmGet(key, fields.distinct: _*)
            .map(decoder.decode)

      override def hmSet[A](key: String, value: A)(implicit
        encoder: Encoder[A]
      ): P[Unit] =
        logger.info(s"hmSet: $key") *>
          commands
            .hmSet(key, encoder.encode(value))

      override def hGet(key: String, field: String): P[Option[String]] =
        logger.info(s"hGet: $key") *>
          commands.hGet(key, field)

      override def hSet(key: String, field: String, value: String): P[Unit] =
        logger.info(s"hSet: $key") *>
          commands.hSet(key, field, value)

      override def hDel(key: String, fields: String*): P[Unit] =
        logger.info(s"hDel: $key") *>
          commands
            .hDel(key, fields.distinct: _*)

      override def expire(key: String, expiration: FiniteDuration): P[Unit] =
        logger.info(s"expire: $key") *>
          commands.expire(key, expiration)
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
