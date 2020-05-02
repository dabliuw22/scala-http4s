package com.leysoft.products.adapter

import cats.implicits._
import ciris.ConfigValue
import ciris._
import ciris.refined._
import enumeratum.{CirisEnum, Enum, EnumEntry}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.collection.{MaxSize, MinSize}
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.W
import eu.timepit.refined.types.net.UserPortNumber

import scala.concurrent.duration._

package object config {

  sealed trait Environment extends EnumEntry

  object Environment extends Enum[Environment] with CirisEnum[Environment] {

    case object Local extends Environment

    case object Testing extends Environment

    case object Production extends Environment

    override def values: IndexedSeq[Environment] = findValues
  }

  type AuthSecretKey = String Refined MinSize[W.`10`.T]

  type AuthAlgorithm = String Refined MaxSize[W.`5`.T]

  type AuthExpirationSeconds = FiniteDuration

  final case class AuthConfiguration(secretKey: Secret[AuthSecretKey],
                                     algorithm: AuthAlgorithm,
                                     expiration: AuthExpirationSeconds)

  type DatabasePassword = String Refined MinSize[W.`5`.T]

  final case class DatabaseConfiguration(
    username: NonEmptyString,
    password: Secret[DatabasePassword]
  )

  final case class ApiConfiguration(
    host: NonEmptyString,
    port: UserPortNumber
  )

  final case class Configuration(
    environment: Environment,
    api: ApiConfiguration,
    database: DatabaseConfiguration,
    auth: AuthConfiguration
  )

  val apiConfig: ConfigValue[ApiConfiguration] =
    (env("API_HOST").as[NonEmptyString].default("localhost"),
     env("API_PORT").as[UserPortNumber].default(8080))
      .parMapN { (host, port) =>
        ApiConfiguration(host, port)
      }

  val databaseConfig: ConfigValue[DatabaseConfiguration] =
    (env("DB_USER").as[NonEmptyString].default("http4s"),
     env("DB_PASSWORD").as[DatabasePassword].default("http4s").secret)
      .parMapN { (user, password) =>
        DatabaseConfiguration(user, password)
      }

  val authConfig: ConfigValue[AuthConfiguration] =
    (env("AUTH_SECRET_KEY")
       .as[AuthSecretKey]
       .default("shg4k58shdgfb3dbdn9024")
       .secret,
     env("AUTH_ALGORITHM").as[AuthAlgorithm].default("HS512"),
     env("AUTH_EXPIRATION_SECONDS")
       .as[PosInt]
       .default(600)
       .map(s => FiniteDuration.apply(s.value, SECONDS)))
      .parMapN(
        (secretKey, algorithm, expiration) =>
          AuthConfiguration(secretKey, algorithm, expiration)
      )

  val config: ConfigValue[Configuration] =
    env("API_ENV")
      .as[Environment]
      .default(Environment.Local)
      .flatMap { env =>
        (apiConfig, databaseConfig, authConfig)
          .parMapN { (api, database, auth) =>
            Configuration(env, api, database, auth)
          }
      }
}
