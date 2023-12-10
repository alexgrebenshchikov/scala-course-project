package ru.man.power.client.model.response

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import ru.man.power.client.model.{ErrorDict, Issue, Price}
import ru.man.power.server.domain.response.ErrorResponse
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

case class FlightDestinationsErrorResponse(errors: Seq[Issue])

object FlightDestinationsErrorResponse {
  implicit val flightDestinationErrorDecoder: Decoder[FlightDestinationsErrorResponse] =
    deriveDecoder[FlightDestinationsErrorResponse]
  def toServerErrorResponse(
      flightDestinationsErrorResponse: FlightDestinationsErrorResponse,
  ): ErrorResponse =
    ErrorResponse(flightDestinationsErrorResponse.errors.map(_.title))
}
