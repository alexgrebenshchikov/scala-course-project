package ru.man.power.repository

import ru.man.power.client.model.response.AccessTokenResponse
import ru.man.power.repository.entities.AccessTokenEntity

trait ExternalApiAccessTokenRepository[F[_]] {
  def getAccessToken: F[Option[AccessTokenEntity]]
  def putAccessToken(accessToken: AccessTokenEntity): F[Long]
}
