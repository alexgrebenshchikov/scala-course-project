package ru.man.power.server.service

import cats.{FlatMap, Monad}
import cats.effect.{Async, IO}
import com.typesafe.config.ConfigFactory
import ru.man.power.client.HttpFlightDestinationsClient
import ru.man.power.client.model.Data
import ru.man.power.client.model.Data.toFavoritesEntity
import ru.man.power.client.model.configuration.FlightDestinationsClientConfiguration
import ru.man.power.client.model.response.AccessTokenResponse.toAccessTokenEntity
import ru.man.power.client.model.response.FlightDestinationsErrorResponse
import ru.man.power.client.model.response.FlightDestinationsErrorResponse.toServerErrorResponse
import ru.man.power.repository.entities.FavoritesEntity.toFavoritesItem
import ru.man.power.repository.entities.SearchParamsEntity.toResponse
import ru.man.power.repository.{
  ExternalApiAccessTokenRepository,
  FavoritesRepository,
  SearchHistoryRepository,
}
import ru.man.power.server.domain.{FavoritesItem, SearchParams, User}
import ru.man.power.server.domain.SearchParams.toEntity
import ru.man.power.server.domain.response.{
  ErrorResponse,
  FavoritesResponse,
  FindFlightDestinationsResponse,
  SearchHistoryResponse,
}
import sttp.client3.{DeserializationException, HttpError}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

trait FlightDestinationsService[F[_]] {
  def findFlightDestinations(
      user: User,
      searchParams: SearchParams,
  ): F[Either[ErrorResponse, FindFlightDestinationsResponse]]

  def getSearchHistory(user: User): F[SearchHistoryResponse]

  def deleteSearchHistory(user: User): F[Unit]

  def addToFavorites(user: User, data: Data): F[Either[ErrorResponse, Unit]]

  def getFavorites(user: User): F[Either[ErrorResponse, FavoritesResponse]]

  def deleteFavorites(user: User): F[Either[ErrorResponse, Unit]]
}

class RepositoryFlightDestinationsService(
    private val tokenRepository: ExternalApiAccessTokenRepository[IO],
    private val searchHistoryRepository: SearchHistoryRepository[IO],
    private val favoritesRepository: FavoritesRepository[IO],
) extends FlightDestinationsService[IO] {
  override def findFlightDestinations(
      user: User,
      searchParams: SearchParams,
  ): IO[Either[ErrorResponse, FindFlightDestinationsResponse]] = {
    val config = ConfigFactory.load()
    val flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration =
      FlightDestinationsClientConfiguration.loadConfig(config)
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
    searchHistoryRepository
      .getUserSearchHistory(user.login)
      .map(s => SearchHistoryResponse(s.map(toResponse)))

  override def deleteSearchHistory(user: User): IO[Unit] =
    searchHistoryRepository.deleteUserSearchHistory(user.login).map(_ => ())

  override def addToFavorites(user: User, data: Data): IO[Either[ErrorResponse, Unit]] =
    favoritesRepository
      .addToFavorites(toFavoritesEntity(user.login, data))
      .map(_.left.map(e => ErrorResponse(Seq(e.getMessage))).map(_ => ()))

  override def getFavorites(user: User): IO[Either[ErrorResponse, FavoritesResponse]] =
    favoritesRepository
      .getFavorites(user.login)
      .map(
        _.left
          .map(_ => ErrorResponse(Seq("Unable to add to favorites")))
          .map(entities => FavoritesResponse(entities.map(toFavoritesItem))),
      )

  override def deleteFavorites(user: User): IO[Either[ErrorResponse, Unit]] =
    favoritesRepository
      .deleteFavorites(user.login)
      .map(_.left.map(_ => ErrorResponse(Seq("Unable to delete favorites"))).map(_ => ()))
}
