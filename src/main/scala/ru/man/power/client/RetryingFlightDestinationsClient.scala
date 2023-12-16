package ru.man.power.client

import cats.MonadThrow
import io.circe
import retry.{Sleep, retryingOnSomeErrors}
import ru.man.power.client.model.response.{AccessTokenResponse, FlightDestinationsErrorResponse, FlightDestinationsResponse}
import ru.man.power.commons.RetryUtils
import ru.man.power.server.domain.SearchParams
import sttp.client3.ResponseException

class RetryingFlightDestinationsClient[F[_]: MonadThrow: Sleep](
    flightDestinationsClient: FlightDestinationsClient[F],
    retryUtils: RetryUtils[F],
) extends FlightDestinationsClient[F] {
  override def findFlightDestinations(searchParams: SearchParams, accessToken: String): F[Either[
    ResponseException[FlightDestinationsErrorResponse, circe.Error],
    FlightDestinationsResponse,
  ]] = retryingOnSomeErrors[Either[
    ResponseException[FlightDestinationsErrorResponse, circe.Error],
    FlightDestinationsResponse,
  ]](
    isWorthRetrying = retryUtils.isTimeoutException,
    policy = retryUtils.policy,
    onError = retryUtils.onError,
  )(flightDestinationsClient.findFlightDestinations(searchParams, accessToken))

  override def renewAccessToken(): F[AccessTokenResponse] = ???
}
