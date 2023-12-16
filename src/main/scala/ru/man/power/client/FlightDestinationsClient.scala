package ru.man.power.client

import cats.effect.kernel.Async
import cats.implicits.toFlatMapOps
import ru.man.power.client.model.configuration.FlightDestinationsClientConfiguration
import ru.man.power.client.model.response.{AccessTokenResponse, FlightDestinationsErrorResponse, FlightDestinationsResponse}
import ru.man.power.commons.SttpResponseUtils
import ru.man.power.server.domain.SearchParams
import sttp.client3.{ResponseException, SttpBackend, UriContext, basicRequest}
import sttp.model.Uri

trait FlightDestinationsClient[F[_]] {
  def findFlightDestinations(
      searchParams: SearchParams,
      accessToken: String,
  ): F[
    Either[
      ResponseException[FlightDestinationsErrorResponse, io.circe.Error],
      FlightDestinationsResponse,
    ],
  ]
}

class HttpFlightDestinationsClient[F[_]: Async](
    sttpBackend: SttpBackend[F, Any],
    flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration,
) extends FlightDestinationsClient[F] {
  def findFlightDestinations(
      searchParams: SearchParams,
      accessToken: String,
  ): F[
    Either[
      ResponseException[FlightDestinationsErrorResponse, io.circe.Error],
      FlightDestinationsResponse,
    ],
  ] = {
    val findFlightDestinationsUrl: Uri =
      uri"${flightDestinationsClientConfiguration.baseUrl}?origin=${searchParams.origin}&departureDate=${searchParams.departureDate}&oneWay=${searchParams.oneWay}&maxPrice=${searchParams.maxPrice}&duration=${searchParams.travelDuration.lower},${searchParams.travelDuration.upper}"
    //uri"${flightDestinationsClientConfiguration.baseUrl}?origin=PAR&maxPrice=200"
    basicRequest
      .get(findFlightDestinationsUrl)
      .header("Authorization", accessToken, replaceExisting = true)
      .response(
        SttpResponseUtils
          .unwrapResponseOrError2[F, FlightDestinationsErrorResponse, FlightDestinationsResponse],
      )
      .readTimeout(flightDestinationsClientConfiguration.timeout)
      .send(sttpBackend)
      .flatMap(_.body)
  }

  def renewAccessToken(): F[AccessTokenResponse] = {
    val renewAccessTokenUrl: Uri =
      uri"${flightDestinationsClientConfiguration.renewTokenUrl}"

    basicRequest
      .post(renewAccessTokenUrl)
      .header("Content-Type", "application/x-www-form-urlencoded")
      .body(
        s"grant_type=client_credentials&client_id=${flightDestinationsClientConfiguration.clientId}" +
          s"&client_secret=${flightDestinationsClientConfiguration.clientSecret}",
      )
      .response(SttpResponseUtils.unwrapResponse[F, AccessTokenResponse])
      .readTimeout(flightDestinationsClientConfiguration.timeout)
      .send(sttpBackend)
      .flatMap(_.body)
  }
}
