package ru.man.power.server.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import ru.man.power.server.domain.response.SearchHistoryResponse
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

final case class TravelDuration(lower: Int, upper: Int)

object TravelDuration {
  implicit val travelDurationEncoder: Encoder[TravelDuration] =
    deriveEncoder[TravelDuration]

  implicit val travelDurationDecoder: Decoder[TravelDuration] =
    deriveDecoder[TravelDuration]

  implicit val travelDurationReader: JsonReader[TravelDuration] = jsonReader

  implicit val travelDurationWriter: JsonWriter[TravelDuration] = jsonWriter

  implicit val travelDurationSchema: Schema[TravelDuration] = Schema.derived
    .description("Длительность путешествия.")
}
