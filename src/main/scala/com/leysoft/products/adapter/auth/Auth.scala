package com.leysoft.products.adapter.auth

import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.leysoft.products.adapter.config.AuthConfiguration
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt._
import org.http4s.server.AuthMiddleware
import pdi.jwt.{JwtAlgorithm, JwtClaim}

object Auth {
  import Codecs._
  import cats.syntax.applicative._
  import cats.syntax.applicativeError._
  import cats.syntax.flatMap._
  import cats.syntax.functor._
  import io.circe.syntax._

  type AuthUserUsername = String

  type AuthUserPassword = String

  case class AuthUser(username: AuthUserUsername, password: AuthUserPassword) {

    def toUser: User = User(username)
  }

  case class AuthUserException(message: String)
      extends RuntimeException(message)

  case class AuthUserNotFoundException(message: String)
      extends RuntimeException(message)

  sealed trait AuthRepository[P[_]] {

    def findBy(username: AuthUserUsername): P[AuthUser]

    def save(user: AuthUser): P[Unit]

    def update(user: AuthUser): P[Unit]
  }

  type Store[P[_]] = Ref[P, Map[AuthUserUsername, AuthUser]]

  final class InMemoryUserRepository[P[_]: Effect] private (store: Store[P])
      extends AuthRepository[P] {

    override def findBy(username: AuthUserUsername): P[AuthUser] =
      store.get
        .map(_.get(username))
        .map {
          case Some(user) => user
          case _ =>
            throw AuthUserNotFoundException(s"Not Found User: $username")
        }

    override def save(user: AuthUser): P[Unit] =
      store.get
        .map(_.get(user.username))
        .flatMap {
          case Some(_) =>
            throw AuthUserException(s"Not Saved User: ${user.username}")
          case _ =>
            store.get.map(_.updated(user.username, user))
        }

    override def update(user: AuthUser): P[Unit] =
      store.get
        .map(_.get(user.username))
        .flatMap {
          case Some(_) => store.get.map(_.updated(user.username, user))
          case _ =>
            throw AuthUserException(s"Not Updated User: ${user.username}")
        }
  }

  object InMemoryUserRepository {

    private val initUsers = Map(
      "username1" -> AuthUser("username1", "password1"),
      "username2" -> AuthUser("username2", "password2"),
      "username3" -> AuthUser("username3", "password3")
    )

    def make[P[_]: Effect]: P[AuthRepository[P]] =
      Ref
        .of(initUsers)
        .map(new InMemoryUserRepository[P](_))
  }

  case class User(username: AuthUserUsername)

  final class AuthService[P[_]: Sync] private (
    val authConfig: AuthConfiguration,
    val authRepository: AuthRepository[P]
  )(implicit val clock: java.time.Clock) {
    import io.circe.parser.decode

    private val secretKey = JwtSecretKey(authConfig.secretKey.value.value)

    private val algorithm = JwtAlgorithm.HS512

    private val jwtAuth = JwtAuth.hmac(secretKey.value, algorithm)

    private val authenticate: JwtToken => JwtClaim => P[Option[User]] =
      token =>
        claim =>
          decode[User](claim.content).toOption
            .pure[P]
            .flatMap {
              case Some(user) =>
                authRepository
                  .findBy(user.username)
                  .map(_.toUser)
                  .map(Option(_))
                  .handleError(_ => None)
              case _ => Sync[P].delay(None)
            }

    val middleware: P[AuthMiddleware[P, User]] =
      Sync[P].delay(
        JwtAuthMiddleware[P, User](jwtAuth, authenticate)
      )

    /*
    def authenticate(token: JwtToken)(claim: JwtClaim): P[Option[User]] =
      decode[User](claim.content).toOption.pure[P]
        .flatMap {
          case Some(user) => authRepository.findBy(user.username)
            .map(_.toUser)
            .map(Option(_))
            .handleError(_ => None)
          case _ => Sync[P].delay(None)
        }
     */

    def create(auth: AuthUser): P[JwtToken] =
      for {
        user <- authRepository.findBy(auth.username)
        claim <- if (user.password.equals(auth.password))
                   Sync[P]
                     .delay(
                       JwtClaim(content = user.toUser.asJson.noSpaces).issuedNow
                         .expiresIn(authConfig.expiration.toSeconds)
                     )
                 else throw AuthUserException("Invalid Credentials")
        token <- jwtEncode[P](claim, secretKey, algorithm)
      } yield token
  }

  object AuthService {

    def make[P[_]: Sync](
      authConfig: AuthConfiguration,
      authRepository: AuthRepository[P]
    ): P[AuthService[P]] =
      Sync[P]
        .delay(java.time.Clock.systemUTC)
        .map(implicit clock => new AuthService[P](authConfig, authRepository))
  }
}
