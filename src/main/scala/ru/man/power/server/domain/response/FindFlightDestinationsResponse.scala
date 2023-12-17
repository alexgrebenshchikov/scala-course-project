package ru.man.power.server.domain.response

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import ru.man.power.client.model.{Data, Links}
import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

final case class FindFlightDestinationsResponse(
    flights: Seq[Data],
)

object FindFlightDestinationsResponse {
  implicit val findFlightDestinationsResponseDecoder: Decoder[FindFlightDestinationsResponse] =
    deriveDecoder[FindFlightDestinationsResponse]

  implicit val findFlightDestinationsResponseReader: JsonReader[FindFlightDestinationsResponse] =
    jsonReader

  implicit val findFlightDestinationsResponseWriter: JsonWriter[FindFlightDestinationsResponse] =
    jsonWriter

  implicit val findFlightDestinationsResponseSchema: Schema[FindFlightDestinationsResponse] =
    Schema.derived
      .description("Данные о направлениях для путешествия.")
}
