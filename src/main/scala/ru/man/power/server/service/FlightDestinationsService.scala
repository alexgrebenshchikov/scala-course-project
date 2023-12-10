package ru.man.power.server.service

import cats.{FlatMap, Monad}
import cats.effect.{Async, IO}
import com.typesafe.config.ConfigFactory
import ru.man.power.client.HttpFlightDestinationsClient
import ru.man.power.client.model.Data
import ru.man.power.client.model.configuration.FlightDestinationsClientConfiguration
import ru.man.power.client.model.response.AccessTokenResponse.toAccessTokenEntity
import ru.man.power.client.model.response.FlightDestinationsErrorResponse
import ru.man.power.client.model.response.FlightDestinationsErrorResponse.toServerErrorResponse
import ru.man.power.repository.entities.SearchParamsEntity.toResponse
import ru.man.power.repository.{ExternalApiAccessTokenRepository, SearchHistoryRepository}
import ru.man.power.server.domain.{SearchParams, User}
import ru.man.power.server.domain.SearchParams.toEntity
import ru.man.power.server.domain.response.{ErrorResponse, FindFlightDestinationsResponse, SearchHistoryResponse}
import sttp.client3.{DeserializationException, HttpError}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

trait FlightDestinationsService[F[_]] {
  def findFlightDestinations(
      user: User,
      searchParams: SearchParams,
  ): F[Either[ErrorResponse, FindFlightDestinationsResponse]]

  //def login(login: String, password: String): F[AuthrorizationResponse]

  def getSearchHistory(user: User): F[SearchHistoryResponse]

  def deleteSearchHistory(user: User): F[Unit]
}

class RepositoryFlightDestinationsService(
    private val tokenRepository: ExternalApiAccessTokenRepository[IO],
    private val searchHistoryRepository: SearchHistoryRepository[IO],
) extends FlightDestinationsService[IO] {
  override def findFlightDestinations(
      user: User,
      searchParams: SearchParams,
  ): IO[Either[ErrorResponse, FindFlightDestinationsResponse]] = {
    val config = ConfigFactory.load()
    val flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration =
      FlightDestinationsClientConfiguration.load(config)
    val asyncBackend = AsyncHttpClientCatsBackend[IO]()

    def findFlightDestinationsInternal(
        updateAccessToken: Boolean = false,
    ): IO[Either[ErrorResponse, FindFlightDestinationsResponse]] =
      for {
        sttpBackend <- asyncBackend
        client = new HttpFlightDestinationsClient(
          sttpBackend,
          flightDestinationsClientConfiguration,
        )
        accessToken <-
          if (updateAccessToken)
            for {
              tokenResponse <- client.renewAccessToken()
              _ <- tokenRepository.putAccessToken(toAccessTokenEntity(tokenResponse))
            } yield tokenResponse.access_token
          else
            for {
              tokenOption <- tokenRepository.getAccessToken
              token = tokenOption.map(_.access_token).getOrElse("token")
            } yield token
        response <- client.findFlightDestinations(searchParams, s"Bearer $accessToken")

        result <- response match {
          case Left(error) =>
            error match {
              case HttpError(body, statusCode) =>
                if (statusCode.code == 401 && !updateAccessToken)
                  findFlightDestinationsInternal(true)
                else IO.pure(Left(toServerErrorResponse(body)))
              case DeserializationException(_, error) =>
                IO.pure(Left(ErrorResponse(Seq(error.getMessage))))
            }
          case Right(value) => IO.pure(Right(FindFlightDestinationsResponse(value.data)))
        }
      } yield result

    for {
      _ <- searchHistoryRepository.putUserSearchHistory(toEntity(user.login, searchParams))
      res <- findFlightDestinationsInternal()
    } yield res
  }

  override def getSearchHistory(user: User): IO[SearchHistoryResponse] =
    searchHistoryRepository.getUserSearchHistory(user.login).map(s => SearchHistoryResponse(s.map(toResponse)))

  override def deleteSearchHistory(user: User): IO[Unit] = searchHistoryRepository.deleteUserSearchHistory(user.login).map(_ => ())
}
