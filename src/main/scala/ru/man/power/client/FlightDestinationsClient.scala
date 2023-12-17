package ru.man.power.client

import cats.Id
import cats.effect.kernel.Async
import cats.implicits.toFlatMapOps
import ru.man.power.client.model.configuration.FlightDestinationsClientConfiguration
import ru.man.power.client.model.response.{AccessTokenResponse, FlightDestinationsErrorResponse, FlightDestinationsResponse}
import ru.man.power.commons.SttpResponseUtils
import ru.man.power.server.domain.SearchParams
import sttp.client3.{ResponseException, SttpBackend, UriContext, basicRequest}
import sttp.model.Uri
import sttp.model.Uri.QuerySegment.KeyValue

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

  def renewAccessToken(): F[AccessTokenResponse]
}

class HttpFlightDestinationsClient[F[_]: Async](
    sttpBackend: SttpBackend[F, Any],
    flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration,
) extends FlightDestinationsClient[F] {
  override def findFlightDestinations(
      searchParams: SearchParams,
      accessToken: String,
  ): F[
    Either[
      ResponseException[FlightDestinationsErrorResponse, io.circe.Error],
      FlightDestinationsResponse,
    ],
  ] = {
    val findFlightDestinationsUrlId: Id[Uri] =
      uri"${flightDestinationsClientConfiguration.baseUrl}"
    val findFlightDestinationsUrl = findFlightDestinationsUrlId
      .flatMap(
        _.addQuerySegments(
          Seq(
            KeyValue("origin", searchParams.origin),
            KeyValue("departureDate", searchParams.departureDate),
            KeyValue("oneWay", s"${searchParams.oneWay}"),
            KeyValue("maxPrice", s"${searchParams.maxPrice}"),
          ),
        ),
      )
      .flatMap(uri =>
        searchParams.travelDuration match {
          case Some(value) =>
            uri.addQuerySegment(
              KeyValue(
                "duration",
                s"${value.lower},${value.upper}",
              ),
            )
          case None => uri
        },
      )
    basicRequest
      .get(findFlightDestinationsUrl)
      .header("Authorization", accessToken, replaceExisting = true)
      .response(
        SttpResponseUtils
          .unwrapResponseOrError[F, FlightDestinationsErrorResponse, FlightDestinationsResponse],
      )
      .readTimeout(flightDestinationsClientConfiguration.timeout)
      .send(sttpBackend)
      .flatMap(_.body)
  }

  override def renewAccessToken(): F[AccessTokenResponse] = {
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
