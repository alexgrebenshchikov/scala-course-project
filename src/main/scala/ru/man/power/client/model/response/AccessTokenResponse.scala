package ru.man.power.client.model.response

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import ru.man.power.repository.entities.AccessTokenEntity

case class AccessTokenResponse(access_token: String)

object AccessTokenResponse {
  implicit val tokenResponseDecoder: Decoder[AccessTokenResponse] =
    deriveDecoder[AccessTokenResponse]

  def toAccessTokenEntity(accessTokenResponse: AccessTokenResponse): AccessTokenEntity = AccessTokenEntity(
    accessTokenResponse.access_token,
  )
}
