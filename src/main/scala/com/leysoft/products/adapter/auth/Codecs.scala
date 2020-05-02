package com.leysoft.products.adapter.auth

import com.leysoft.products.adapter.auth.Auth.{AuthUser, User}
import dev.profunktor.auth.jwt.JwtToken
import io.circe.{Decoder, Encoder, Json}

object Codecs {

  private val USERNAME = "username"

  private val PASSWORD = "password"

  private val ACCESS_TOKEN = "access_token"

  implicit val authUserDecoder: Decoder[AuthUser] =
    Decoder.instance[AuthUser] { cursor =>
      for {
        username <- cursor.downField(USERNAME).as[String]
        password <- cursor.downField(PASSWORD).as[String]
      } yield AuthUser(username, password)
    }

  implicit val userDecoder: Decoder[User] = Decoder.instance[User] { cursor =>
    for {
      username <- cursor.downField(USERNAME).as[String]
    } yield User(username)
  }

  implicit val userEncoder: Encoder[User] = Encoder.instance[User] { user =>
    Json.obj((USERNAME, Json.fromString(user.username)))
  }

  implicit val jwtTokenEncoder: Encoder[JwtToken] = Encoder.instance[JwtToken] {
    token =>
      Json.obj((ACCESS_TOKEN, Json.fromString(token.value)))
  }
}
